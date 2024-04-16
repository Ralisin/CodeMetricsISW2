package it.ralisin.controller;

import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;
import it.ralisin.tools.JsonUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExtractDataFromJira {
    private final String projName;

    public ExtractDataFromJira(String projName) {
        this.projName = projName.toUpperCase();
    }

    public List<Release> extractReleasesList() throws IOException, URISyntaxException {
        List<Release> releaseList = new ArrayList<>();

        String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;

        // Retrieve project data from url and populate release list
        JSONObject json = JsonUtils.readJsonFromUrl(url);
        JSONArray versions = json.getJSONArray("versions");
        for (int i = 0; i < versions.length(); i++) {
            String releaseName;
            String releaseDate;

            JSONObject releaseJsonObj = versions.getJSONObject(i);
            if (releaseJsonObj.has("releaseDate") && releaseJsonObj.has("name")) {
                releaseName = releaseJsonObj.get("name").toString();
                releaseDate = releaseJsonObj.get("releaseDate").toString();

                releaseList.add(new Release(releaseName, LocalDate.parse(releaseDate).atStartOfDay()));
            }
        }

        // Sort releases via data
        releaseList.sort(Comparator.comparing(Release::getDate));

        // Set release id
        int i = 0;
        for (Release release : releaseList) {
            release.setId(++i);
        }

        return releaseList;
    }

    public List<Ticket> extractTicketsList(List<Release> releaseList) throws IOException, URISyntaxException {
        final int maxResults = 1000;
        int i = 0, j, total;

        List<Ticket> ticketList = new ArrayList<>();

        do {
            //Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
            j = i + maxResults;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22" +
                    projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR" +
                    "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,versions,created&startAt="
                    + i + "&maxResults=" + j;

            JSONObject json = JsonUtils.readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            for (; i < total && i < j; i++) {
                //Iterate through each bug
                String ticketKey = issues.getJSONObject(i % maxResults).get("key").toString();

                JSONObject fields = issues.getJSONObject(i % maxResults).getJSONObject("fields");

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

                LocalDateTime creationDate = ZonedDateTime.parse(fields.getString("created"), formatter).toLocalDateTime();
                Release openingVersion = getReleaseAfterOrEqualDate(creationDate, releaseList);

                LocalDateTime resolutionDate = ZonedDateTime.parse(fields.getString("resolutiondate"), formatter).toLocalDateTime();
                Release fixedVersion = getReleaseAfterOrEqualDate(resolutionDate, releaseList);

                // OV is not before or equal to FV
                if (creationDate.isAfter(resolutionDate)) {
                    continue;
                }

                Ticket ticket = new Ticket(ticketKey, creationDate, resolutionDate, openingVersion, fixedVersion);

                JSONArray affectedVersionArray = fields.getJSONArray("versions");
                List<Release> affectedVersionList = getAffectedVersions(affectedVersionArray, releaseList);

                if (!affectedVersionList.isEmpty() &&
                        affectedVersionList.getFirst().getDate().isBefore(resolutionDate) &&
                        !affectedVersionList.getFirst().getDate().isAfter(creationDate)) {
                    ticket.setAffectedVersions(affectedVersionList);
                }

                ticketList.add(ticket);
            }
        } while (i < total);

        // ticketList.sort(Comparator.comparing(Ticket::getResolutionDate));

        return ticketList;
    }

    private Release getReleaseAfterOrEqualDate(LocalDateTime date, List<Release> releaseList) {
        releaseList.sort(Comparator.comparing(Release::getDate));

        for (Release release : releaseList) {
            if (!release.getDate().isBefore(date)) {
                return release;
            }
        }

        return null;
    }

    private List<Release> getAffectedVersions(JSONArray affectedVersionArray, List<Release> releaseList) {
        List<Release> affectedVersions = new ArrayList<>();

        for (int i = 0; i < affectedVersionArray.length(); i++) {
            JSONObject affectedVersionJsonObject = affectedVersionArray.getJSONObject(i);
            if (!affectedVersionJsonObject.has("releaseDate") || !affectedVersionJsonObject.has("name")) {
                continue;
            }

            String affectedReleaseName = affectedVersionJsonObject.get("name").toString();

            for(Release release : releaseList) {
                if (affectedReleaseName.equals(release.getName())) affectedVersions.add(release);
            }
        }

        return affectedVersions;
    }
}



















