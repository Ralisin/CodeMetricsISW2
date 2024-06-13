package it.ralisin;

import it.ralisin.controller.Metrics;
import it.ralisin.controller.Weka;

public class Main {
    private static final String SRC_DIR = "src/main/resources/";

    public static void main(String[] args) throws Exception {
        Weka weka;

        Metrics.dataExtraction("BOOKKEEPER", "https://github.com/Ralisin/bookkeeper");
        weka = new Weka("BOOKKEEPER", SRC_DIR);
        weka.wekaAnalyses();

        Metrics.dataExtraction("STORM", "https://github.com/Ralisin/storm");
        weka = new Weka("STORM", SRC_DIR);
        weka.wekaAnalyses();
    }
}
