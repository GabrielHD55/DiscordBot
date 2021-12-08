package com.vyrimbot.Tasks;

import com.vyrimbot.Main;
import com.vyrimbot.Tickets.Ticket;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CloseTask extends TimerTask {

    private final Ticket ticket;
    private final TextChannel textChannel;

    public CloseTask(Ticket ticket) {
        this.ticket = ticket;
        this.textChannel = this.ticket.getTextChannel();

        new Thread(() -> {
            Timer timer = new Timer();
            timer.schedule(this, TimeUnit.MINUTES.toMillis(1L));
        }).start();
    }

    public CloseTask(TextChannel textChannel) {
        this.ticket = null;
        this.textChannel = textChannel;

        new Thread(() -> {
            Timer timer = new Timer();
            timer.schedule(this, TimeUnit.MINUTES.toMillis(1L));
        }).start();
    }

    @Override
    public void run() {
        if (this.ticket != null) {

        }

        Main.getTicketManager().delete(this.textChannel);
    }
}
