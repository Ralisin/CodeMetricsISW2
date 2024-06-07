package it.ralisin;

import it.ralisin.controller.Metrics;

public class Main {
    public static void main(String[] args) throws Exception {
        Metrics.dataExtraction("BOOKKEEPER", "https://github.com/Ralisin/bookkeeper");

//        Metrics.dataExtraction("STORM", "https://github.com/Ralisin/storm");
    }
}
