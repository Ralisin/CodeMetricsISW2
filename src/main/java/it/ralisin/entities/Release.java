package it.ralisin.entities;

import java.time.LocalDateTime;

public class Release {
    private int id;
    private final String releaseName;
    private final LocalDateTime releaseDate;

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

    public void setId(int newId) {
        this.id = newId;
    }

    @Override
    public String toString() {
        return releaseName + ", " + releaseDate;
    }
}
