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
        ExtractDataFromJira dataFromJira = new ExtractDataFromJira(projName);

        List<Release> releaseList = dataFromJira.extractReleasesList();
        List<Ticket> ticketList = dataFromJira.extractTicketsList(releaseList);

        TicketsTool.fixInconsistentTickets(ticketList, releaseList);
        ticketList.sort(Comparator.comparing(Ticket::getCreationDate));

        for (Ticket ticket : ticketList) {
            System.out.println(ticket);
        }

        System.out.println(ticketList.toArray().length);
    }
}
