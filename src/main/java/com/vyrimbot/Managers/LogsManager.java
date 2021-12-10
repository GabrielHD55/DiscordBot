package com.vyrimbot.Managers;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import org.simpleyaml.configuration.file.YamlFile;

public class LogsManager {

    private final TextChannel channel;

    public LogsManager() {
        YamlFile config = Main.getInstance().getConfig();

        this.channel = Main.getInstance().getJda().getTextChannelById(config.getLong("LogChannel"));
    }

    public void sendDeleteMessageLog(GuildMessageDeleteEvent event) {
        Message message = event.getChannel().getHistory().getMessageById(event.getMessageIdLong());

        Main.debug("INFO", event.getResponseNumber()+" ");
        Main.debug("INFO", event.getGuild().getMemberById(event.getResponseNumber()).getAsMention());
        if(message == null) {
            Main.debug("ERROR", "null");
            return;
        }

        Member member = message.getMember();

        EmbedBuilder embed = EmbedUtil.getEmbed();
        embed.setTitle("**");
    }

    public void sendUserBanned(User user, User admin, String reason) {

    }
}
