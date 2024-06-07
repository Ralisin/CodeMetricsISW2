package it.ralisin.tools;

import it.ralisin.entities.JavaClass;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaClassTool {
    private JavaClassTool() {}

    public static JavaClass getJavaClass(TreeWalk treeWalk, Repository repository) {
        try {
            ObjectId objectId = treeWalk.getObjectId(0);
            ObjectLoader loader = repository.open(objectId);

            byte[] bytes = loader.getBytes();

            return new JavaClass(treeWalk.getPathString(), new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.SEVERE, "Error while extracting Java class ", e);
        }

        return null;
    }

    public static List<String> getModifiedClasses(RevCommit commit, Repository repository) throws IOException {
        List<String> modifiedClasses = new ArrayList<>();

        try(DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
            ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser newTree = new CanonicalTreeParser();

            ObjectId objectIdNew = commit.getTree();
            newTree.reset(reader, objectIdNew);
            RevCommit commitParent = commit.getParent(0);

            CanonicalTreeParser oldTree = new CanonicalTreeParser();
            ObjectId objectIdOld = commitParent.getTree();
            oldTree.reset(reader, objectIdOld);
            diffFormatter.setRepository(repository);
            List<DiffEntry> entries = diffFormatter.scan(oldTree, newTree);

            for(DiffEntry entry : entries) {
                if(entry.getNewPath().contains(".java") && !entry.getNewPath().contains("/test/"))
                    modifiedClasses.add(entry.getNewPath());
            }
        }

        return modifiedClasses;
    }
}
