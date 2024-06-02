package it.ralisin.controller;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.tools.CommitTool;
import it.ralisin.tools.GitFactory;
import it.ralisin.tools.JavaClassTool;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitExtractor {
    final Git git;
    final Repository repository;

    public GitExtractor(String projName, String repoURL) throws GitAPIException, IOException {
        GitFactory gitFactory = new GitFactory(projName, repoURL);
        this.git = gitFactory.getGit();
        this.repository = gitFactory.getGit().getRepository();
    }

    public List<RevCommit> getAllCommits() throws IOException {
        List<RevCommit> commitList = new ArrayList<>();

        // Get all commits
        try {
            Iterable<RevCommit> allCommits = git.log().all().call();
            for (RevCommit commit : allCommits) {
                commitList.add(commit);
            }
        } catch (GitAPIException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while extracting commits", e);
        }

        return commitList;
    }

    // Set JavaClasses for every release
    public void getClasses(List<Release> releases) {
        for (Release release : releases) {
            List<String> classPathList = new ArrayList<>();
            for (RevCommit commit : release.getCommitList()) {
                extractJavaClasses(commit, release, classPathList);
            }
        }
    }

    private void extractJavaClasses(RevCommit commit, Release release, List<String> classPathList) {
        try {
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(repository);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);

            while (treeWalk.next()) {
                String path = treeWalk.getPathString();
                if (path.endsWith(".java") && !classPathList.contains(path) && !path.contains("/test/")) {
                    classPathList.add(path);
                    JavaClass javaClass = JavaClassTool.getJavaClass(treeWalk, repository);

                    release.getJavaClassList().add(javaClass);
                }
            }
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while extracting Java paths", e);
        }
    }

    public void linkCommitsToClasses(List<JavaClass> javaClassList, List<RevCommit> commitList, List<Release> releaseList) throws IOException {
        for (RevCommit commit : commitList) {
            Release commitRelease = CommitTool.getCommitRelease(commit, releaseList);
            if (commitRelease != null) {
                List<String> classPathList = JavaClassTool.getModifiedClasses(commit, repository);
                for (String classes : classPathList) {
                    CommitTool.assignCommitToClass(javaClassList, classes, commit);
                }
            }
        }
    }
}
