package it.ralisin.controller;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.TicketsTool;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;

public class Metrics {
    public static void dataExtraction(String projName) throws IOException, URISyntaxException {
        ExtractDataFromJira jira = new ExtractDataFromJira(projName);

        // Get release list from jira
        List<Release> releaseList = jira.extractReleasesList();

        // Get ticket list from jira
        List<Ticket> ticketList = jira.extractTicketsList(releaseList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        Proportion.calculateProportion(releaseList, ticketList);
    }
}
