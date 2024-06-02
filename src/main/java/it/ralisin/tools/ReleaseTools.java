package it.ralisin.tools;

import it.ralisin.entities.Release;
import org.eclipse.jgit.revwalk.RevCommit;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReleaseTools {
    private ReleaseTools() {}

    public static Release getReleaseAfterOrEqualDate(LocalDateTime date, List<Release> releaseList) {
        if (releaseList == null || releaseList.isEmpty()) {
            return null;
        }

        for (Release release : releaseList) {
            if (!release.getDate().isBefore(date)) {
                return release;
            }
        }

        return null;
    }

    public static List<Release> getAffectedVersions(JSONArray affectedVersionArray, List<Release> releaseList) {
        List<Release> affectedVersions = new ArrayList<>();

        if (affectedVersionArray == null || releaseList == null) {
            return affectedVersions;
        }

        for (int i = 0; i < affectedVersionArray.length(); i++) {
            JSONObject affectedVersionJsonObject = affectedVersionArray.getJSONObject(i);
            if (!affectedVersionJsonObject.has("releaseDate") || !affectedVersionJsonObject.has("name")) {
                continue;
            }

            String affectedReleaseName = affectedVersionJsonObject.getString("name");

            for (Release release : releaseList) {
                if (affectedReleaseName.equals(release.getName())) affectedVersions.add(release);
            }
        }

        affectedVersions.sort(Comparator.comparing(Release::getDate));

        return affectedVersions;
    }

    public static void linkCommits(List<RevCommit> revCommitList, List<Release> releaseList) {
        for (RevCommit commit : revCommitList) {
            Release commitRelease = getCommitsRelease(commit, releaseList);

            if (commitRelease != null) commitRelease.getCommitList().add(commit);
        }
    }

    private static Release getCommitsRelease(RevCommit commit, List<Release> releaseList) {
        LocalDateTime commitDate = commit.getAuthorIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        for (Release r : releaseList) {
            if (commitDate.isBefore(r.getDate())) return r;
            else if (commitDate.isAfter(releaseList.getLast().getDate())) return releaseList.getLast();
        }

        return null;
    }
}
