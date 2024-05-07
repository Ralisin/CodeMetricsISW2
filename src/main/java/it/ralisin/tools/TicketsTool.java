package it.ralisin.tools;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class TicketsTool {
    private TicketsTool() {}

    public static void fixInconsistentTickets(List<Ticket> ticketList, List<Release> releaseList) {
        // Filter tickets and setTicketIV for possible tickets
        ticketList.removeIf(ticket -> {
            if (ticket.getOV() == null || ticket.getFV() == null)
                return true; // Remove if any of the required data is missing

            if (ticket.getAVList().isEmpty()) return false;

            setTicketIV(ticket);

            return ticket.getAVList().getFirst().getDate().isAfter(ticket.getCreationDate());
        });

        // Remove tickets if:
        ticketList.removeIf(ticket ->
                // date of OV is not after first available release in releaseList
                !ticket.getOV().getDate().isAfter(releaseList.getFirst().getDate()) ||
                        // date of OV is after date of FV
                        ticket.getOV().getDate().isAfter(ticket.getFV().getDate()) ||
                        // date of OV is the same of first available release in releaseList
                        Objects.equals(ticket.getOV().getName(), releaseList.getFirst().getName())
        );
    }

    // Check the validity of release in affected version's list and set ticket injected version
    private static void setTicketIV(Ticket ticket) {
        // Ticket data
        LocalDateTime ticketCreationDate = ticket.getCreationDate();
        LocalDateTime ticketResolutionDate = ticket.getResolutionDate();
        Release openingVersion = ticket.getOV();

        // First affected version data
        Release firstAffectedVersion = ticket.getAVList().getFirst();
        LocalDateTime firstAVDate = firstAffectedVersion.getDate();

        // Date of first AV is not before resolution date
        if (!firstAVDate.isBefore(ticketResolutionDate)) return;
        // Date of first AV is after creation date
        if (firstAVDate.isAfter(ticketCreationDate)) return;
        // Name of first AV is the same of ticket opening version
        if (Objects.equals(firstAffectedVersion.getName(), openingVersion.getName())) return;

        ticket.setIV(firstAffectedVersion);
    }

    public static void linkCommits(List<Ticket> ticketList, List<RevCommit> revCommitList) {
        for (Iterator<Ticket> iterator = ticketList.iterator(); iterator.hasNext(); ) {
            Ticket ticket = iterator.next();
            for (RevCommit revCommit : revCommitList) {
                if (revCommit.getFullMessage().contains(ticket.getKey())) ticket.addCommit(revCommit);
            }

            if (ticket.getCommitList().isEmpty()) {
                iterator.remove();
            }
        }
    }
}
