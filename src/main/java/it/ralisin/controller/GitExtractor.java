package it.ralisin.controller;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.tools.GitFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GitExtractor {
    final GitFactory gitFactory;

    public GitExtractor(String projName, String repoURL) throws IOException, GitAPIException {
        this.gitFactory = new GitFactory(projName, repoURL);
    }

    public void deleteDirectory(File directory) {
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

        Git git = gitFactory.getGit();

        // Get all commits
        try (Repository ignored = git.getRepository()) {
            Iterable<RevCommit> allCommits = git.log().all().call();
            for (RevCommit commit : allCommits) {
                commitList.add(commit);
            }
        }

        return commitList;
    }

    public void extractJavaFiles(List<Release> releaseList) throws IOException, GitAPIException {
        System.out.println("Extracting Java Files");

        int releaseCount = 0;
        for (Release release : releaseList) {
            System.out.print("\r\rRelease (" + releaseCount + ", " + releaseList.size() + ")");
            System.out.println();
            releaseCount++;

            List<JavaClass> touchedJavaClassList = release.getJavaClassList();
            Set<String> classPathSet = new HashSet<>();

            int commitCount = 0;
            for (RevCommit commit : release.getCommitList()) {
                System.out.print("\r\tRevCommit (" + commitCount + ", " + release.getCommitList().size() + ")");
                commitCount++;

                ObjectId treeId = commit.getTree();
                try (TreeWalk treeWalk = new TreeWalk(gitFactory.getGit().getRepository())) {
                    treeWalk.reset(treeId);
                    treeWalk.setRecursive(true);
                    while (treeWalk.next()) {
                        String classPath = treeWalk.getPathString();

                        if (classPath.endsWith(".java") && !classPath.contains("/test/")) {
                            if (classPathSet.add(classPath)) {
                                String fileContent = getFileContent(commit, classPath);

                                JavaClass javaClass = new JavaClass(classPath, fileContent);

                                if (checkCommitTouchesClass(commit, classPath))
                                    javaClass.addCommit(commit);

                                touchedJavaClassList.add(javaClass);
                            } else {
                                // Add commit to a javaClass that already exist
                                for (JavaClass javaClass : touchedJavaClassList) {
                                    if (javaClass.getClassPath().equals(classPath) && checkCommitTouchesClass(commit, classPath)) {
                                        javaClass.addCommit(commit);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            System.out.println("Number of different class touched per release " + release + ": " + classPathSet.size());
            int counter = 0;
            for (JavaClass jc : touchedJavaClassList) {
                System.out.println(release.getName() + ", javaClassPath: " + jc.getClassPath() + ", commits: " + jc.getCommitList().size());
                counter++;
            }
            System.out.println("counter: " + counter);
        }
    }

    private String getFileContent(RevCommit commit, String filePath) throws IOException, GitAPIException {
        Git git = gitFactory.getGit();

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

    private boolean checkCommitTouchesClass(RevCommit commit, String classFilePath) throws IOException, GitAPIException {
        try (Repository repository = gitFactory.getGit().getRepository();
             Git git = new Git(repository);
             RevWalk revWalk = new RevWalk(repository)) {

            RevCommit parentCommit = revWalk.parseCommit(commit.getParent(0).getId());

            // Prendi i tree parser per il commit e il suo genitore
            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            oldTreeParser.reset(repository.newObjectReader(), parentCommit.getTree().getId());
            CanonicalTreeParser newTreeParser = new CanonicalTreeParser();
            newTreeParser.reset(repository.newObjectReader(), commit.getTree().getId());

            // Ottieni la lista delle differenze tra il commit e il suo genitore
            List<DiffEntry> diffs = git.diff()
                    .setOldTree(oldTreeParser)
                    .setNewTree(newTreeParser)
                    .call();

            // Controlla se una delle differenze coinvolge il file della classe
            for (DiffEntry diff : diffs) {
                if (diff.getNewPath().equals(classFilePath)) {
                    return true;
                }
            }
        }

        return false;
    }
}
