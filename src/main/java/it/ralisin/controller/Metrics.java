package it.ralisin.controller;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.TicketsTool;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Metrics {
    public static void dataExtraction(String projName) throws IOException, URISyntaxException {
        ExtractDataFromJira jira = new ExtractDataFromJira(projName);

        // Get release list from jira
        List<Release> releaseList = jira.extractReleasesList();

        for (Release r : releaseList) {
            System.out.println(r);
        }

        // Get ticket list from jira
        List<Ticket> ticketList = jira.extractTicketsList(releaseList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        for (Ticket ticket : ticketList) {
            if (Objects.equals(ticket.getKey(), "BOOKKEEPER-291")) {
                System.out.println(ticket);
            }
        }

        Proportion.proportion(releaseList, ticketList);
        TicketsTool.fixInconsistentTickets(ticketList, releaseList); 

        for (Ticket ticket : ticketList) {
            System.out.println(ticket);
        }
    }
}
