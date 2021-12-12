package com.vyrimbot.Managers;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import org.simpleyaml.configuration.file.YamlFile;

public class LogsManager {

    @Getter private TextChannel channelP;
    @Getter private TextChannel channelR;

    public LogsManager() {
        setChannels();
    }
    
    public void setChannels() {
    	YamlFile config = Main.getInstance().getConfig();

        this.channelP = Main.getInstance().getJda().getTextChannelById(config.getString("LogsChannelP"));
        this.channelR = Main.getInstance().getJda().getTextChannelById(config.getString("LogsChannelR"));
    }

}
