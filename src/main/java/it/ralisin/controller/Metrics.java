package it.ralisin.controller;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.ReleaseTools;
import it.ralisin.tools.TicketsTool;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Metrics {
    public static void dataExtraction(String projName, String gitHubUrl) throws IOException, URISyntaxException, GitAPIException {
        JiraDataExtractor jira = new JiraDataExtractor(projName);

        // Get release list from jira
        List<Release> releaseList = jira.extractReleasesList();

        // Get ticket list from jira
        List<Ticket> ticketList = jira.extractTicketsList(releaseList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        // Do proportion on tickets
        Proportion.proportion(releaseList, ticketList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList); // Remove inconsistent tickets after the proportion if any

        // TODO write tickets in a csv file

        // Get list of full project commits
        Logger.getAnonymousLogger().log(Level.INFO, "Cloning repository from GitHub " + gitHubUrl);
        GitCommitsExtractor gitExtractor = new GitCommitsExtractor(gitHubUrl);
        List<RevCommit> commitList = gitExtractor.getAllCommits();

        // Link commits to release
        ReleaseTools.linkCommits(commitList, releaseList);
        releaseList.removeIf(release -> release.getRevCommitList().isEmpty()); // Remove releases with empty commit list
        for(int i = 1; i <= releaseList.size(); i++) releaseList.get(i - 1).setId(i); // Reassign release id

        // Remove half releases
        int halfReleases = releaseList.size() / 2;
        releaseList.removeIf(release -> release.getId() > halfReleases);

        // TODO write releases in a csv file

        // Link tickets to relative commits
        TicketsTool.linkCommits(ticketList, commitList);


    }
}
