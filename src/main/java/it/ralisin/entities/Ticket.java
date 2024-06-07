package it.ralisin.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private final String key;
    private final LocalDateTime creationDate;
    private final LocalDateTime resolutionDate;
    private final Release openingVersion;
    private final Release fixedVersion;
    private Release injectedVersion;
    private List<Release> affectedVersions;

    private final List<RevCommit> commits = new ArrayList<>();

    public Ticket(String key, LocalDateTime creationDate, LocalDateTime resolutionDate, Release openingVersion, Release fixedVersion, List<Release> affectedVersions) {
        this.key = key;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
        this.affectedVersions = affectedVersions;
    }

    public String getKey() {
        return key;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getResolutionDate() {
        return resolutionDate;
    }

    public Release getIV() {
        return injectedVersion;
    }

    public void setIV(Release injectedVersion) {
        this.injectedVersion = injectedVersion;
    }

    public Release getOV() {
        return openingVersion;
    }

    public Release getFV() {
        return fixedVersion;
    }

    public List<Release> getAVList() {
        return affectedVersions;
    }

    public void setAVList(List<Release> affectedVersions) {
        this.affectedVersions = affectedVersions;
    }

    public List<RevCommit> getCommitList() {
        return commits;
    }

    public void addCommit(RevCommit commits) {
        this.commits.add(commits);
    }

    @Override
    public String toString() {
        return String.format("Ticket key: %s, IV: %s, OV: %s, FV: %s, AV: %s, creationDate: %s, resolutionDate: %s", key, injectedVersion, openingVersion, fixedVersion, affectedVersions, creationDate, resolutionDate);
    }
}