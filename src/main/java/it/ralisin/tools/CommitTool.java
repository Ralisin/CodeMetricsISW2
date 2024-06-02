package it.ralisin.tools;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class CommitTool {
    private CommitTool() {

    }

    public static List<RevCommit> getCommitsInTicketList(List<RevCommit> commitList, List<Ticket> ticketList) {
        List<RevCommit> commitInTicketList = new ArrayList<>();

        for (RevCommit commit : commitList) {
            for (Ticket ticket : ticketList) {
                if (ticket.getCommitList().contains(commit) && !commitInTicketList.contains(commit)) {
                    commitInTicketList.add(commit);
                }
            }
        }

        return commitInTicketList;
    }

    public static Release getCommitRelease(RevCommit commit, List<Release> releaseList) {
        for (Release release : releaseList) {
            for (RevCommit releaseCommit : release.getCommitList()) {
                if (commit.getId().equals(releaseCommit.getId()))
                    return release;
            }
        }

        return null;
    }

    public static void assignCommitToClass(List<JavaClass> javaClassList, String classModified, RevCommit commit) {
        for (JavaClass javaClass : javaClassList) {
            if (javaClass.getClassPath().equals(classModified) && !javaClass.getCommitList().contains(commit)) {
                javaClass.getCommitList().add(commit);
            }
        }
    }
}
