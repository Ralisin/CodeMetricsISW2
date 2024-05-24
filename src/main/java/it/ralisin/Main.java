package it.ralisin;

import it.ralisin.controller.Metrics;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException, GitAPIException, InterruptedException {
        Metrics.dataExtraction("BOOKKEEPER", "https://github.com/Ralisin/bookkeeper");
//        Metrics.dataExtraction("STORM", "https://github.com/Ralisin/storm");
    }
}
