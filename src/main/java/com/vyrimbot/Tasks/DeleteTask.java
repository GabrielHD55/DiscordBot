package com.vyrimbot.Tasks;

import com.vyrimbot.Utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class DeleteTask extends TimerTask {

    private final String finalLimit;
    private final GuildMessageReceivedEvent event;
    private int limit;
    private Message lastMessage;

    public DeleteTask(String finalLimit, int limit, GuildMessageReceivedEvent event) {
        this.limit = limit;
        this.event = event;
        this.finalLimit = finalLimit;

        new Thread(() -> {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(this, 0L, TimeUnit.SECONDS.toMillis(5L));
        }).start();
    }

    @Override
    public void run() {
        if (this.lastMessage != null) this.lastMessage.delete().queue();

        if (this.limit < 0) {
            if (this.lastMessage != null) this.lastMessage.delete().queue();
            this.cancel();
            return;
        }

        if (this.limit > 100) {
            List<Message> messages = this.event.getChannel().getHistory().retrievePast(100).complete();
            this.event.getChannel().deleteMessages(messages).queue();
            this.limit -= 100;
            EmbedBuilder process = EmbedUtil.getEmbed(this.event.getAuthor());
            process.setColor(0x22ff2a);
            process.setTitle("✅ Deleting messages...");
            this.lastMessage = this.event.getChannel().sendMessage(process.build()).complete();
        } else if (this.limit > 0 && this.limit <= 100) {
            List<Message> messages = this.event.getChannel().getHistory().retrievePast(this.limit).complete();
            this.event.getChannel().deleteMessages(messages).queue();
            this.limit = 0;
            EmbedBuilder process = EmbedUtil.getEmbed(this.event.getAuthor());
            process.setColor(0x22ff2a);
            process.setTitle("✅ Deleting messages...");
            this.lastMessage = this.event.getChannel().sendMessage(process.build()).complete();
        } else if (this.limit == 0) {
            EmbedBuilder success = EmbedUtil.getEmbed(this.event.getAuthor());
            success.setColor(0x22ff2a);
            success.setTitle("✅ " + this.finalLimit + " Messages deleted successfully.");
            this.lastMessage = event.getChannel().sendMessage(success.build()).complete();
            this.limit--;
        }
    }
}
