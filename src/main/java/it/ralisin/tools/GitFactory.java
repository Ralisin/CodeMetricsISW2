package it.ralisin.tools;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.TextProgressMonitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GitFactory {
    private final String projName;
    private final String repoURL;

    public GitFactory(String projName, String repoURL) {
        this.projName = projName;
        this.repoURL = repoURL;
    }

    public Git getGit() throws IOException, GitAPIException {
        Path repoPath = Paths.get("src/main/resources/repo/" + projName);

        if (!Files.exists(repoPath)) {
            // Create new directory for the repository
            Files.createDirectories(repoPath);

            // Clone repository
            ProgressMonitor monitor = new TextProgressMonitor(new PrintWriter(System.out));
            return Git.cloneRepository()
                    .setURI(repoURL)
                    .setDirectory(repoPath.toFile())
                    .setProgressMonitor(monitor)
                    .call();
        }

        return Git.open(repoPath.toFile());
    }
}
