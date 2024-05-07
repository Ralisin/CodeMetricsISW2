package it.ralisin.tools;

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
}
