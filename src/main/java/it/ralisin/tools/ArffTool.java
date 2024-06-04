package it.ralisin.tools;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArffTool {
    final String projName;
    final String dirPath;

    final String TRAINING = "/training/";
    final String TESTING = "/testing/";

    final String CSV = "/csv/";
    final String ARFF = "arff";

    public ArffTool(String projName, String dirPath) throws IOException {
        this.projName = projName;

        Path tempDir = Paths.get(dirPath);
        if (!Files.exists(tempDir)) Files.createDirectories(tempDir);

        this.dirPath = dirPath;
    }

    public void csvToArff() {
        convertCsvDirectory(dirPath + projName + TRAINING + CSV);
        convertCsvDirectory(dirPath + projName + TESTING + CSV);
    }

    private void convertCsvDirectory(String dirPath) {
        try {
            Path dir = Paths.get(dirPath);

            Path arffDir = dir.getParent().resolve(ARFF);
            if (!Files.exists(arffDir)) Files.createDirectories(arffDir);

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file: stream) {
                    if (!file.toString().endsWith(".csv")) continue;

                    String arffFileName = replaceCsvToArffExtension(file.getFileName().toString());
                    if (!arffFileName.endsWith(".arff")) throw new IOException("Error converting file extension: " + arffFileName);

                    convertCsvFile(file.toString(), arffDir + "/" + arffFileName);
                }
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, "Error converting csv to arff: " + e.getMessage());
        }
    }

    private void convertCsvFile(String srcFile, String destFile) throws Exception {
        // Load CSV file
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(srcFile));
        Instances data = loader.getDataSet();

        // Remove release ID column and classPath column
        Remove remove = new Remove();
        remove.setAttributeIndices("1,2");
        remove.setInputFormat(data);

        // Save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(destFile));
        saver.writeBatch();
    }

    private String replaceCsvToArffExtension(String filePath) {
        final String csvExtension = ".csv";
        final String arffExtension = ".arff";

        if (filePath.endsWith(csvExtension)) {
            return filePath.substring(0, filePath.length() - csvExtension.length()) + arffExtension;
        } else {
            return filePath;
        }
    }
}
