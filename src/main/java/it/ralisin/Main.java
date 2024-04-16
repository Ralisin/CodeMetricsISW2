package it.ralisin;

import it.ralisin.controller.ExtractDataFromJira;
import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ExtractDataFromJira dataFromJira = new ExtractDataFromJira("BOOKKEEPER");

        List<Release> releaseList = dataFromJira.extractReleasesList();
        List<Ticket> ticketList = dataFromJira.extractTicketsList(releaseList);

        for(Release release : releaseList) {
            System.out.println(release);
        }

        for(Ticket ticket : ticketList) {
            if (ticket.getInjectedVersion() != null)
                System.out.println(ticket);
        }

        System.out.println(ticketList.size());
    }
}
