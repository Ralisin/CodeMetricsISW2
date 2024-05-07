package it.ralisin.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Release {
    private int id;
    private final String releaseName;
    private final LocalDateTime releaseDate;
    private final List<RevCommit> commitList = new ArrayList<>();

    public Release(String releaseName, LocalDateTime releaseDate) {
        this.releaseName = releaseName;
        this.releaseDate = releaseDate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return releaseName;
    }

    public LocalDateTime getDate() {
        return releaseDate;
    }

    public List<RevCommit> getCommitList() {
        return commitList;
    }

    public void setId(int newId) {
        this.id = newId;
    }

    @Override
    public String toString() {
        return releaseName + " - " + releaseDate;
    }
}
