package it.ralisin.controller;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.TicketsTool;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proportion {
    private enum OtherProjectNames {
        AVRO,
        SYNCOPE,
        OPENJPA,
        TAJO,
        ZOOKEEPER
    }

    private Proportion() {}

    public static void calculateProportion(List<Release> releaseList, List<Ticket> ticketList) throws IOException, URISyntaxException {
        float pColdStart = coldStartProportion();

        Logger.getAnonymousLogger().log(Level.INFO, "pColdStart" + pColdStart);
    }

    private static float coldStartProportion() throws IOException, URISyntaxException {
        List<Float> projectsProportion = new ArrayList<>();

        for(OtherProjectNames projectName : OtherProjectNames.values()) {
            Logger.getAnonymousLogger().log(Level.INFO, "ColdStart on project: " + projectName);

            ExtractDataFromJira jira = new ExtractDataFromJira(projectName.toString());

            // Get release list from jira
            List<Release> releaseList = jira.extractReleasesList();

            // Get ticket list from jira
            List<Ticket> ticketList = jira.extractTicketsList(releaseList);
            TicketsTool.fixInconsistentTickets(ticketList, releaseList);
            ticketList.removeIf(ticket -> ticket.getIV() == null);

            // Calculate project proportion
            List<Float> proportionList = new ArrayList<>();
            for(Ticket ticket : ticketList) {
                Float P = computeProportion(ticket);
                proportionList.add(P);
            }

            projectsProportion.add(proportionList.stream().reduce(0f, Float::sum)/proportionList.size());
        }

        return projectsProportion.stream().reduce(0f, Float::sum)/projectsProportion.size();
    }

    private static float computeProportion(Ticket ticket) {
        /*
        Formula: P = (FV − IV)/(FV − OV)

        From paper:
             if FV=OV set FV - OV = 1 -> P = FV - IV
         */

        if (ticket.getFV().getId() == ticket.getOV().getId()) return (ticket.getFV().getId() - ticket.getIV().getId());

        return (ticket.getFV().getId() - ticket.getIV().getId()) * 1.0f/(ticket.getFV().getId()-ticket.getOV().getId());
    }
}
