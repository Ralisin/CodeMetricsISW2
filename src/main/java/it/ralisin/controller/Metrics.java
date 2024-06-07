package it.ralisin.controller;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.*;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metrics {
    private static final Logger logger = Logger.getLogger(Metrics.class.getName());

    private static final String SRC_DIR = "src/main/resources/";

    private Metrics() {}

    public static void dataExtraction(String projName, String gitHubUrl) throws Exception {
        CsvTool csvTool = new CsvTool(projName, SRC_DIR + projName);

        JiraDataExtractor jira = new JiraDataExtractor(projName);

        // Get release list from jira
        List<Release> releaseList = jira.extractReleasesList();

        csvTool.csvReleaseFile(releaseList);

        // Get ticket list from jira
        List<Ticket> ticketList = jira.extractTicketsList(releaseList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        // Do proportion on tickets
        Proportion.proportion(releaseList, ticketList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList); // Remove inconsistent tickets after the proportion if any

        csvTool.csvTicketFile(ticketList);

        // Get list of full project commits
        logger.log(Level.INFO, "Cloning repository from GitHub %s", gitHubUrl);
        GitExtractor gitExtractor = new GitExtractor(projName, gitHubUrl);
        List<RevCommit> commitList = gitExtractor.getAllCommits();

        // Link commits to release
        logger.info( "Linking commits to releases");
        ReleaseTools.linkCommits(commitList, releaseList);
        releaseList.removeIf(release -> release.getCommitList().isEmpty()); // Remove releases with empty commit list
        for(int i = 1; i <= releaseList.size(); i++) releaseList.get(i - 1).setId(i); // Reassign release id

        // Remove half releases
        int halfReleases = releaseList.size() / 2;
        List<Release> halfReleaseList = new ArrayList<>(releaseList);
        halfReleaseList.removeIf(release -> release.getId() > halfReleases);

        // Link tickets to relative commits
        TicketsTool.linkCommits(ticketList, commitList);

        // Set JavaClasses for every release
        logger.info("Extracting JavaClasses from Git");
        gitExtractor.getClasses(releaseList);

        logger.info("Linking commits to relative javaClass");
        for (Release release : releaseList)
            gitExtractor.linkCommitsToClasses(release.getJavaClassList(), commitList, releaseList);

        List<RevCommit> commitInTicketList = CommitTool.getCommitsInTicketList(commitList, ticketList);

        logger.info("Evaluating metrics for every javaClass");
        for (Release release : releaseList) {
            for (JavaClass javaClass : release.getJavaClassList()) {
                MetricEvaluator compMetrics = new MetricEvaluator(javaClass, commitInTicketList, gitExtractor.repository);
                compMetrics.evaluateMetrics();
            }
        }

        // Walk forward
        logger.info("Generating dataset with WalkForward");
        BugginessEvaluator bugEval = new BugginessEvaluator(gitExtractor.repository);
        for (int i = 2; i <= halfReleaseList.size(); i++) {
            int limitReleaseId = i;

            List<Release> walkRelease = halfReleaseList.stream().filter(release -> release.getId() <= limitReleaseId).toList();
            List<Ticket> walkTicket = ticketList.stream().filter(ticket -> ticket.getFV().getId() <= walkRelease.getLast().getId()).toList();

            bugEval.evaluateBagginess(walkRelease, walkTicket);
            csvTool.csvDatasetFile(walkRelease, limitReleaseId, true);

            List<Release> testingRelease = Collections.singletonList(releaseList.get(limitReleaseId));

            bugEval.evaluateBagginess(releaseList, ticketList);
            csvTool.csvDatasetFile(testingRelease, limitReleaseId, false);
        }

        // Convert csv files to arff files
        logger.info("Converting CSV files to ARFF files");
        ArffTool arffTool = new ArffTool(projName, SRC_DIR);
        arffTool.csvToArff();

        String trainingFolder = SRC_DIR + projName + "/training/arff/";
        String testingFolder = SRC_DIR + projName + "/testing/arff/";

        // Train machine learning models
        logger.info("Weka");
        Weka weka = new Weka(trainingFolder, testingFolder, releaseList, csvTool);
        weka.wekaAnalyses();
    }
}
