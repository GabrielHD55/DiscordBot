package com.vyrimbot.Logs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.simpleyaml.configuration.file.YamlFile;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;

import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class Log {
	
	LogType type;
	String message;
	
	public Log(LogType type, String message) {
		this.type = type;
		this.message = message;
		
		
		createLog();
		
	}
	
	void createLog() {
		YamlFile config = Main.getInstance().getConfig();
		
		TextChannel channelP = Main.getInstance().getJda().getTextChannelById(config.getString("LogsChannelP"));
		TextChannel channelR = Main.getInstance().getJda().getTextChannelById(config.getString("LogsChannelR"));
		
		EmbedBuilder embed = EmbedUtil.getEmbed();
        embed.setTitle("**");
        embed.setDescription(message);
        embed.addField("", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), false);
        
        if(type == LogType.Root) {
        	channelR.sendMessageEmbeds(embed.build()).queue();
        	return;
        }
        
        channelP.sendMessageEmbeds(embed.build()).queue();
    	return;
      
	}

}
