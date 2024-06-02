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

public class CSVWriter {
    final String filePath;

    public CSVWriter(String filePath) throws IOException {
        Path tempDir = Paths.get(filePath);
        if (!Files.exists(tempDir)) Files.createDirectories(tempDir);

        this.filePath = filePath;
    }

    public void csvReleaseFile(List<Release> releaseList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + "/releaseList.csv"))) {
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + "/ticketList.csv"))) {
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

    public void csvJavaClassFile(List<Release> releaseList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + "/dataset.csv"))) {
            writer.write("Release ID, Filepath, Size, LOC Touched, NR, NFix, NAuth, LOC Added, Max LOC Added, Average LOC Added, Churn, Max Churn, Average Churn");
            writer.newLine();

            for (Release release : releaseList) {
                for (JavaClass javaClass : release.getJavaClassList()) {
                    // Release ID
                    writer.write(release.getId() + ",");
                    // Filepath
                    writer.write(javaClass.getClassPath() + ",");
                    // LOC
                    writer.write(javaClass.getSize() + ",");
                    // locTouched
                    writer.write(javaClass.getLocTouched() + ",");
                    // nr
                    writer.write(javaClass.getNr() + ",");
                    // nFix
                    writer.write(javaClass.getNFix() + ",");
                    // nAuth
                    writer.write(javaClass.getNAuth() + ",");
                    // locAdded
                    writer.write(javaClass.getLocAdded() + ",");
                    // maxLocAdded
                    writer.write(javaClass.getMaxLocAdded() + ",");
                    // averageLocAdded
                    writer.write(javaClass.getAvgLocAdded() + ",");
                    // churn
                    writer.write(javaClass.getChurn() + ",");
                    // maxChurn
                    writer.write(javaClass.getMaxChurn() + ",");
                    // averageChurn
                    writer.write(javaClass.getAvgChurn() + "");


                    writer.newLine();
                }
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.INFO, e.getMessage());
        }
    }
}
