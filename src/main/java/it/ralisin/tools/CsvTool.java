package it.ralisin.tools;

import it.ralisin.entities.JavaClass;
import it.ralisin.entities.Release;
import it.ralisin.entities.Ticket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvTool {
    final String projName;
    final String dirPath;

    final String CSV = "/csv/";

    final String TRAINING = "/training/";
    final String TESTING = "/testing/";
    final String INFO = "/info/";

    public CsvTool(String projName, String filePath) throws IOException {
        this.projName = projName;

        Path tempDir = Paths.get(filePath);
        if (!Files.exists(tempDir)) Files.createDirectories(tempDir);

        this.dirPath = filePath;
    }

    public void csvReleaseFile(List<Release> releaseList) {
        String filePath = dirPath + INFO + "/releaseList.csv";

        try {
            Files.createDirectories(Paths.get(filePath).getParent());
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());

            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("ID, Name, Date");
            writer.newLine();

            for (Release release : releaseList) {
                writer.write(release.getId() + ",");
                writer.write(release.getName() + ",");
                writer.write(release.getDate().toString());

                writer.newLine();
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }
    }

    public void csvTicketFile(List<Ticket> ticketList) {
        String filePath = dirPath + INFO + "/ticketList.csv";

        try {
            Files.createDirectories(Paths.get(filePath).getParent());
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());

            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("KEY, IV, OV, FV, AV List");
            writer.newLine();

            for (Ticket ticket : ticketList) {
                writer.write(ticket.getKey() + ",");
                writer.write(ticket.getIV().getName() + ",");
                writer.write(ticket.getOV().getName() + ",");
                writer.write(ticket.getFV().getName() + ",");

                StringBuilder AVList = new StringBuilder("\"{");
                int size = ticket.getAVList().size();
                for (int i = 0; i < size; i++) {
                    Release release = ticket.getAVList().get(i);
                    AVList.append(release.getName());
                    if (i < size - 1) {
                        AVList.append(", ");
                    }
                }
                AVList.append("}\"");

                writer.write(String.valueOf(AVList));

                writer.newLine();
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }
    }

    public void csvDatasetFile(List<Release> releaseList, int walk, boolean isTraining) {
        FileWriter fileWriter = null;

        String filePath;

        if(isTraining)
                filePath = dirPath + TRAINING + CSV + projName + "trainingSet_" + walk + ".csv";
        else
            filePath = dirPath + TESTING + CSV + projName + "testingSet_" + walk + ".csv";

        try {
            // Create directory if it doesn't exist
            Files.createDirectories(Paths.get(filePath).getParent());

            fileWriter = new FileWriter(filePath);
            fileWriter.append("Release ID, Filepath, Size, LOC Touched, NR, NFix, NAuth, LOC Added, Max LOC Added, Average LOC Added, Churn, Max Churn, Average Churn, Bugginess");
            fileWriter.append("\n");

            for (Release release : releaseList) {
                for (JavaClass javaClass : release.getJavaClassList())
                    writeJavaClass(fileWriter, release.getId(), javaClass);
            }
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        } finally {
            closeFileWrite(fileWriter);
        }
    }

    private void writeJavaClass(FileWriter fileWriter, int releaseId, JavaClass javaClass) throws IOException {
        String str;

        // Release ID
        str = releaseId + ",";
        // Filepath
        str += javaClass.getClassPath() + ",";
        // LOC
        str += javaClass.getSize() + ",";
        // locTouched
        str += javaClass.getLocTouched() + ",";
        // nr
        str += javaClass.getNr() + ",";
        // nFix
        str += javaClass.getNFix() + ",";
        // nAuth
        str += javaClass.getNAuth() + ",";
        // locAdded
        str += javaClass.getLocAdded() + ",";
        // maxLocAdded
        str += javaClass.getMaxLocAdded() + ",";
        // averageLocAdded
        str += javaClass.getAvgLocAdded() + ",";
        // churn
        str += javaClass.getChurn() + ",";
        // maxChurn
        str += javaClass.getMaxChurn() + ",";
        // averageChurn
        str += javaClass.getAvgChurn() + ",";
        // bugginess
        str += javaClass.getBuggyness();

        fileWriter.append(str);
        fileWriter.append("\n");
    }

    private void closeFileWrite(FileWriter fileWriter) {
        if (fileWriter == null) return;

        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }
    }
}
