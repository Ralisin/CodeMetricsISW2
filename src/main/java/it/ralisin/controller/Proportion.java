package it.ralisin.controller;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.TicketsTool;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Proportion {
    private static final Logger logger = Logger.getLogger(Proportion.class.getName());

    private enum OtherProjectNames {
        AVRO,
        SYNCOPE,
        OPENJPA,
        TAJO,
        ZOOKEEPER
    }

    // From paper page 13, in Proportion_Incremental, (ii)
    private static final int THRESHOLD = 5;

    private Proportion() {}

    public static void proportion(List<Release> releaseList, List<Ticket> ticketList) throws IOException, URISyntaxException {
        float pColdStart = coldStartProportion();

        List<Ticket> ticketsForEvaluation = new ArrayList<>();
        for(Ticket ticket : ticketList) {
            if (ticket.getIV() != null) ticketsForEvaluation.add(ticket);
            else computeTicketProportion(ticket, ticketsForEvaluation, releaseList, pColdStart);
        }
    }

    private static void computeTicketProportion(Ticket ticket, List<Ticket> ticketsForEvaluation, List<Release> releaseList, float pColdStart) {
        float proportion;
        if (ticketsForEvaluation.size() < THRESHOLD) {
            proportion = pColdStart;
        } else {
            proportion = incrementalProportion(ticketsForEvaluation);
        }

        setTicketIV(ticket, releaseList, proportion);
        setTicketAV(ticket, releaseList);
    }

    private static void setTicketIV(Ticket ticket, List<Release> releaseList, float proportion) {
        int newIV;
        if (ticket.getFV().getId() == ticket.getOV().getId()) {
            newIV = (int) ( ticket.getFV().getId() - proportion);
        } else {
            // From paper: IV = (FV − OV) ∗ P_Increment
            newIV = (int) (ticket.getFV().getId() - (ticket.getFV().getId() - ticket.getOV().getId()) * proportion);
        }

        if (newIV < 1) newIV = 1;

        if (releaseList.get(newIV-1).getId() == ticket.getOV().getId() && newIV > 1)
            ticket.setIV(releaseList.get(newIV - 2));
        else
            ticket.setIV(releaseList.get(newIV-1));
    }

    private static void setTicketAV(Ticket ticket, List<Release> releaseList) {
        List<Release> avList = new ArrayList<>();

        for (int i = ticket.getIV().getId(); i < ticket.getFV().getId(); i++) {
            avList.add(releaseList.get(i-1));
        }

        ticket.setAVList(avList);
    }

    private static float incrementalProportion(List<Ticket> ticketList) {
        List<Float> proportionList = new ArrayList<>();
        for (Ticket ticket : ticketList) {
            Float ticketProportion = evaluateTicketProportion(ticket);
            proportionList.add(ticketProportion);
        }

        return proportionList.stream().reduce(0f, Float::sum)/proportionList.size();
    }

    private static float coldStartProportion() throws IOException, URISyntaxException {
        List<Float> projectsProportion = new ArrayList<>();

        for(OtherProjectNames projectName : OtherProjectNames.values()) {
            logger.log(Level.INFO,"ColdStart proportion on project: %s", projectName);

            JiraDataExtractor jira = new JiraDataExtractor(projectName.toString());

            // Get release list from jira
            List<Release> releaseList = jira.extractReleasesList();

            // Get ticket list from jira
            List<Ticket> ticketList = jira.extractTicketsList(releaseList);
            TicketsTool.fixInconsistentTickets(ticketList, releaseList);
            ticketList.removeIf(ticket -> ticket.getIV() == null);

            // Calculate project proportion
            List<Float> proportionList = new ArrayList<>();
            for(Ticket ticket : ticketList) {
                Float ticketProportion = evaluateTicketProportion(ticket);
                proportionList.add(ticketProportion);
            }

            projectsProportion.add(proportionList.stream().reduce(0f, Float::sum)/proportionList.size());
        }

        return projectsProportion.stream().reduce(0f, Float::sum)/projectsProportion.size();
    }

    private static float evaluateTicketProportion(Ticket ticket) {
        /*
        Formula: P = (FV − IV)/(FV − OV)

        From paper page 12, in Proportion_ColdStart, (i)
             if FV=OV set FV - OV = 1 -> P = FV - IV
         */

        if (ticket.getFV().getId() == ticket.getOV().getId()) return (ticket.getFV().getId() - ticket.getIV().getId());

        return (ticket.getFV().getId() - ticket.getIV().getId()) * 1.0f/(ticket.getFV().getId()-ticket.getOV().getId());
    }
}
