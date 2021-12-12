package com.vyrimbot.Logs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.simpleyaml.configuration.file.YamlFile;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;

import net.dv8tion.jda.api.EmbedBuilder;

public class Log {
	
	LogType type;
	String message;
	
	public Log(LogType type, String message) {
		this.type = type;
		this.message = message;
		
		
		
		if(Main.getLogsManager().getChannelR() != null && Main.getLogsManager().getChannelP() != null) {
			createLog();
		}
		else {
			System.out.println("Log channels have been not set!!!");
			Main.getLogsManager().setChannels();
		}
		
	}
	
	void createLog() {
		YamlFile config = Main.getInstance().getConfig();
		
		EmbedBuilder embed = EmbedUtil.getEmbed();
        embed.setTitle("**");
        embed.setDescription(message);
        embed.addField("", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), false);
        
        if(type == LogType.Root) {
        	Main.getLogsManager().getChannelR().sendMessageEmbeds(embed.build()).queue();
        	return;
        }
        
        Main.getLogsManager().getChannelP().sendMessageEmbeds(embed.build()).queue();
    	return;
      
	}

}
