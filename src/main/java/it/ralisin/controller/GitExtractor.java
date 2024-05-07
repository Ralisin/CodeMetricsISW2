package it.ralisin.controller;

import it.ralisin.entities.Release;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitExtractor {
    final String repoURL;
    final Git git;

    public GitExtractor(String projName, String url) throws IOException, GitAPIException {
        this.repoURL = url;

        Path tempDir = Paths.get("src/main/resources/repo/" + projName);
        if (!Files.exists(tempDir)) {
            // Create new directory for the repository
            Files.createDirectories(tempDir);

            // Clone repository
            ProgressMonitor monitor = new TextProgressMonitor(new PrintWriter(System.out));
            this.git = Git.cloneRepository()
                    .setURI(this.repoURL)
                    .setDirectory(tempDir.toFile())
                    .setProgressMonitor(monitor)
                    .call();
        } else
            this.git = Git.open(tempDir.toFile());

    }

    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        boolean deleted = directory.delete();
        if (!deleted) {
            // Log failure to delete the directory
            Logger.getAnonymousLogger().log(Level.INFO, "Could not delete " + directory);
        }
    }

    public List<RevCommit> getAllCommits() throws IOException, GitAPIException {
        List<RevCommit> commitList = new ArrayList<>();

        // Get all commits
        try (Repository ignored = git.getRepository()) {
            Iterable<RevCommit> allCommits = git.log().all().call();
            for (RevCommit commit : allCommits) {
                commitList.add(commit);
            }
        }

        return commitList;
    }

    public void extractJavaFiles(List<Release> releaseList) throws IOException {
        for (Release release : releaseList) {

            for (RevCommit commit : release.getCommitList()) {
                ObjectId treeId = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {

                    treeWalk.reset(treeId);
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        if (treeWalk.getPathString().endsWith(".java")) {
                            String fileContent = getFileContent(commit, treeWalk.getPathString());
                            System.out.println(fileContent);

                            return;
                        }
                    }
                }
            }
        }
    }

    private String getFileContent(RevCommit commit, String filePath) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));
            while (treeWalk.next()) {
                if (!treeWalk.isSubtree() && treeWalk.getPathString().equals(filePath)) {
                    git.getRepository().open(treeWalk.getObjectId(0)).copyTo(out);
                    return out.toString();
                }
            }
        }

        return "";
    }

    private int countLinesOfCode(String content) {
        return content.split("\r\n|\r|\n").length;
    }
}
