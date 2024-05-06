package it.ralisin.entities;

import org.eclipse.jgit.revwalk.RevCommit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private final String key;
    private final LocalDateTime creationDate;
    private final LocalDateTime resolutionDate;
    private final Release OV;
    private final Release FV;
    private Release IV;
    private List<Release> AV;

    private final List<RevCommit> commits = new ArrayList<>();

    public Ticket(String key, LocalDateTime creationDate, LocalDateTime resolutionDate, Release OV, Release FV, List<Release> AV) {
        this.key = key;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
        this.OV = OV;
        this.FV = FV;
        this.AV = AV;
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
        return IV;
    }

    public void setIV(Release IV) {
        this.IV = IV;
    }

    public Release getOV() {
        return OV;
    }

    public Release getFV() {
        return FV;
    }

    public List<Release> getAVList() {
        return AV;
    }

    public void setAVList(List<Release> AV) {
        this.AV = AV;
    }

    public List<RevCommit> getCommitList() {
        return commits;
    }

    public void addCommit(RevCommit commits) {
        this.commits.add(commits);
    }

    @Override
    public String toString() {
        return String.format("Ticket key: %s, IV: %s, OV: %s, FV: %s, AV: %s, creationDate: %s, resolutionDate: %s", key, IV, OV, FV, AV, creationDate, resolutionDate);
    }
}