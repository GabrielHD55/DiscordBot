package com.vyrimbot.Managers;

import com.vyrimbot.Tasks.CloseTask;
import com.vyrimbot.Tickets.Ticket;
import com.vyrimbot.Tickets.TicketType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class TicketManager {

    private final Map<Long, Ticket> tickets = new HashMap<>();
    private final Map<Long, CloseTask> closeTickets = new HashMap<>();

    public void createNewTicket(Member member, TicketType types) {
        Ticket ticket = new Ticket(member, types);
        ticket.createNewTicket();

        this.tickets.put(ticket.getTextChannel().getIdLong(), ticket);
    }

    public void loadExistTicket(Member member, TicketType types, TextChannel textChannel) {
        Ticket ticket = new Ticket(member, types);
        ticket.setTextChannel(textChannel);

        this.tickets.put(ticket.getTextChannel().getIdLong(), ticket);
    }

    public Ticket getPlayerTicket(Member member, TicketType types) {
        for (Ticket ticket : this.tickets.values()) {
            if (ticket.getMember().equals(member) && ticket.getType() == types) {
                return ticket;
            }
        }
        return null;
    }

    public Map<Long, Ticket> getTickets() {
        return tickets;
    }

    public Ticket getTicket(TextChannel textChannel) {
        return this.tickets.get(textChannel.getIdLong());
    }

    public void deleteTicket(TextChannel textChannel) {
        Ticket ticket = this.getTicket(textChannel);
        CloseTask closeTask;
        if (ticket != null) {
            closeTask = new CloseTask(ticket);

        } else {
            closeTask = new CloseTask(textChannel);

        }
        this.closeTickets.put(textChannel.getIdLong(), closeTask);
    }

    public void delete(TextChannel textChannel) {
        this.closeTickets.remove(textChannel.getIdLong());
        this.tickets.remove(textChannel.getIdLong());
        textChannel.delete().queue();
    }
}
