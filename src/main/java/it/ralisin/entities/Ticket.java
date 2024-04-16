package it.ralisin.entities;

import java.time.LocalDateTime;
import java.util.List;

public class Ticket {
    private final String key;
    private final LocalDateTime creationDate;
    private final LocalDateTime resolutionDate;
    private Release injectedVersion;
    private final Release openingVersion;
    private final Release fixedVersion;
    private List<Release> affectedVersions;

    public Ticket(String key, LocalDateTime creationDate, LocalDateTime resolutionDate, Release openingVersion, Release fixedVersion) {
        this.key = key;
        this.creationDate = creationDate;
        this.resolutionDate = resolutionDate;
        this.openingVersion = openingVersion;
        this.fixedVersion = fixedVersion;
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

    public Release getInjectedVersion() {
        return injectedVersion;
    }

    public Release getOpeningVersion() {
        return openingVersion;
    }

    public Release getFixedVersion() {
        return fixedVersion;
    }

    public List<Release> getAffectedVersions() {
        return affectedVersions;
    }

    public void setAffectedVersions(List<Release> affectedVersions) {
        this.affectedVersions = affectedVersions;

        this.injectedVersion = affectedVersions.getFirst();
    }

    @Override
    public String toString() {
        return String.format("Ticket key: %s, IV: %s, OV: %s, FV: %s", key, injectedVersion, openingVersion, fixedVersion);
    }
}