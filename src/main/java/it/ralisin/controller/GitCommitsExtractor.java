package it.ralisin.controller;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitCommitsExtractor {
    final String repoURL;

    public GitCommitsExtractor(String url) {
        this.repoURL = url;
    }

    public List<RevCommit> getAllCommits() throws IOException, GitAPIException {
        // Create a temporary directory
        Path tempDir = Files.createTempDirectory("git_temp_repo");

        List<RevCommit> commitList = new ArrayList<>();

        Logger.getAnonymousLogger().log(Level.INFO, "Cloning repository from " + this.repoURL);

        // Clone the repository into the temporary directory
        try (Git git = Git.cloneRepository()
                .setURI(this.repoURL)
                .setDirectory(tempDir.toFile())
                .call()) {
            Logger.getAnonymousLogger().log(Level.INFO, "Parsing commits");

            // Get all commits
            try (Repository ignored = git.getRepository()) {
                Iterable<RevCommit> allCommits = git.log().all().call();
                for (RevCommit commit : allCommits) {
                    commitList.add(commit);
                }
            }
        }

        return commitList;
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
}
