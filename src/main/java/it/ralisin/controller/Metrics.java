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

public class Metrics {
    public static void dataExtraction(String projName, String gitHubUrl) throws IOException, URISyntaxException, GitAPIException {
        JiraDataExtractor jira = new JiraDataExtractor(projName);

        // Get release list from jira
        List<Release> releaseList = jira.extractReleasesList();
        // TODO write releases in a csv file

        // Get ticket list from jira
        List<Ticket> ticketList = jira.extractTicketsList(releaseList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        Proportion.proportion(releaseList, ticketList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        // TODO write tickets in a csv file

        GitCommitsExtractor gitExtractor = new GitCommitsExtractor(gitHubUrl);
        List<RevCommit> commitList = gitExtractor.getAllCommits();

        ReleaseTools.linkCommits(commitList, releaseList);
        // Remove releases with empty commit list
        releaseList.removeIf(release -> release.getRevCommitList().isEmpty());
        // Reassign release id
        for(int i = 1; i <= releaseList.size(); i++) releaseList.get(i - 1).setId(i);

        for (Release r : releaseList) {
            System.out.println(r + ", numCommits: " + r.getRevCommitList().size());
        }

        TicketsTool.linkCommits(ticketList, commitList);

        for (Ticket ticket : ticketList) {
            System.out.println("numCommits: " + ticket.getCommitList().size() + ", " + ticket);
        }
    }
}
