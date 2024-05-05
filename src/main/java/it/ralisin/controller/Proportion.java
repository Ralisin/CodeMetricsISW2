package it.ralisin.controller;

import it.ralisin.entities.Ticket;

import java.util.List;

public class Proportion {
    private Proportion() {}

    public static void proportion(List<Ticket> ticketList) {
        // Case OV=IV
        for (Ticket ticket : ticketList) {
            if (ticket.getIV() == null &&
                    ticket.getOV().getId() == ticket.getFV().getId()) {
                ticket.setIV(ticket.getFV()); // IV = FX
            } else {
                // TODO proportion
            }
        }
    }
}
