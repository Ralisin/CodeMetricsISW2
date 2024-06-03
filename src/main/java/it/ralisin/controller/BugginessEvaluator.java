package it.ralisin.controller;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.JavaClassTool;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BugginessEvaluator {
    private final Repository repository;

    public BugginessEvaluator(Repository repository) {
        this.repository = repository;
    }

    public void evaluateBagginess(List<Release> releaseList, List<Ticket> ticketList) throws IOException {
        for (Release release : releaseList) {
            for (JavaClass javaClass : release.getJavaClassList())
                javaClass.setBugginess(false);
        }

        for (Ticket ticket : ticketList) {
            List<Release> avReleaseList = new ArrayList<>(ticket.getAVList());

            for (Release release : avReleaseList) {
                setJavaClassBugginess(release, ticket);
            }
        }
    }

    private void setJavaClassBugginess(Release release, Ticket ticket) throws IOException {
        for(RevCommit commit : ticket.getCommitList()) {
            List<String> classPathList = JavaClassTool.getModifiedClasses(commit, repository);

            for (JavaClass javaClass : release.getJavaClassList()) {
                if (classPathList.contains(javaClass.getClassPath()))
                    javaClass.setBugginess(true);
            }
        }
    }


}
