package it.ralisin;

import it.ralisin.controller.Metrics;

import java.io.IOException;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Metrics.dataExtraction("BOOKKEEPER");
    }
}
