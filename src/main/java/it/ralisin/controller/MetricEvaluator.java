package it.ralisin.controller;

import it.ralisin.entities.JavaClass;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetricEvaluator {
    private final JavaClass javaClass;
    private final List<RevCommit> commitList;
    private final Repository repository;

    public MetricEvaluator(JavaClass javaClass, List<RevCommit> commitInTicketList, Repository repository) {
        this.javaClass = javaClass;
        this.commitList = commitInTicketList;
        this.repository = repository;
    }

    public void evaluateMetrics() throws IOException {
        setNFix();
        setAuthors();
        setClassSize();
        setLOCMetrics();
    }

    private void setNFix() {
        int count = 0;
        for (RevCommit commit : javaClass.getCommitList())
            if (commitList.contains(commit)) count++;

        javaClass.setNFix(count);
    }

    private void setAuthors() {
        for (RevCommit commit : javaClass.getCommitList()) javaClass.addAuthor(commit.getAuthorIdent().getName());
    }

    private void setClassSize() {
        String[] size = javaClass.getClassContent().split("\r\n|\r|\n");
        javaClass.setSize(size.length);
    }

    private void setLOCMetrics() throws IOException {
        List<Integer> addedLines = new ArrayList<>();
        List<Integer> deletedLines = new ArrayList<>();

        for (RevCommit commit : javaClass.getCommitList()) {
            try (DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
                RevCommit parentCommit = commit.getParent(0);

                df.setRepository(repository);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                List<DiffEntry> diffEntryList = df.scan(parentCommit.getTree(), commit.getTree());

                for (DiffEntry diffEntry : diffEntryList) {
                    if (diffEntry.getNewPath().equals(javaClass.getClassPath())) {
                        int line = 0;
                        int delLine = 0;
                        for (Edit edit : df.toFileHeader(diffEntry).toEditList()) {
                            line += edit.getEndB() - edit.getBeginB();
                            delLine += edit.getEndA() - edit.getBeginA();
                        }
                        addedLines.add(line);
                        deletedLines.add(delLine);
                    }
                }
            }
        }

        locAddedMetrics(javaClass, addedLines);
        churnMetrics(javaClass, addedLines, deletedLines);
        locTouched(javaClass, addedLines, deletedLines);
    }

    private void locAddedMetrics(JavaClass javaClass, List<Integer> addedLines) {
        int maxLOC = 0;
        int sumLines = 0;

        for (Integer line : addedLines) {
            sumLines += line;

            if (line > maxLOC)
                maxLOC = line;
        }

        javaClass.setLocAdded(sumLines);
        javaClass.setMaxLocAdded(maxLOC);
        javaClass.setAvgLocAdded( 1.0 * sumLines / addedLines.size());
    }

    private void churnMetrics(JavaClass javaClass, List<Integer> addedLines, List<Integer> deletedLines) {
        int churnSum = 0;
        int maxChurn = 0;

        for (int i = 0; i < addedLines.size(); i++) {
            int churn = addedLines.get(i) - deletedLines.get(i);

            churnSum += churn;
            if (churn > maxChurn)
                maxChurn = churn;
        }

        javaClass.setChurn(churnSum);
        javaClass.setMaxChurn(maxChurn);
        javaClass.setAvgChurn(1.0 * churnSum / addedLines.size());
    }

    private void locTouched(JavaClass javaClass, List<Integer> addedLines, List<Integer> deletedLines) {
        int totAddedLines = 0;
        int totDeletedLines = 0;

        for (Integer line : addedLines)
            totAddedLines += line;

        for (Integer line : deletedLines)
            totDeletedLines += line;

        javaClass.setLocTouched(totAddedLines + totDeletedLines);
    }
}
