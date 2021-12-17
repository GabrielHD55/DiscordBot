package com.vyrimbot.Logs;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;

import net.dv8tion.jda.api.EmbedBuilder;

public class Log {
	
	LogType type;
	String message;
	String title;
	
	public Log(LogType type, String message, String title) {
		this.type = type;
		this.message = message;
		this.title = title;
		
		
		
		if(Main.getLogsManager().getChannelR() != null && Main.getLogsManager().getChannelP() != null) {
			createLog();
		}
		else {
			System.out.println("Log channels have been not set!!!");
			Main.getLogsManager().setChannels();
		}
		
	}
	
	void createLog() {
		EmbedBuilder embed = EmbedUtil.getEmbed();
        embed.setTitle(title);
        embed.setDescription(message);
        
        if(type == LogType.Root) {
        	Main.getLogsManager().getChannelR().sendMessageEmbeds(embed.build()).queue();
        	return;
        }
        
        Main.getLogsManager().getChannelP().sendMessageEmbeds(embed.build()).queue();
    	return;
      
	}

}
