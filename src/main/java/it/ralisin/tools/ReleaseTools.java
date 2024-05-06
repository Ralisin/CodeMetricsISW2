package it.ralisin.tools;

import it.ralisin.entities.Release;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReleaseTools {
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
}
