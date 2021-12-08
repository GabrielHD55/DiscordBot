package com.vyrimbot.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.vyrimbot.Main;

import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class Giveaway {

	@Getter private final String messageId;
	@Getter private final short winnersAmount;
	@Getter private final LocalDateTime expirationDate;
	@Getter private final TextChannel channel;
	@Getter private final String[] args;
	
	public Giveaway(String messageId, String expirationDate, short winnersAmount, TextChannel channel, String[] args) {
		this.messageId = messageId;
		this.expirationDate = getExpirationDate(expirationDate);
		this.winnersAmount = winnersAmount;
		this.channel = channel;
		this.args = args;
		
		Main.getInstance().getGiveaways().add(this);
	}
	
	public static LocalDateTime getExpirationDate(String timeToAdd) {
		LocalDateTime now = LocalDateTime.now();
		String[] timeUnits = timeToAdd.split(" ");
		
		for(String s : timeUnits) {
			if(s.endsWith("w")) {
				now = now.plusWeeks(Long.parseLong(s.replaceAll("[^0-9.]", "")));
			}
			
			if(s.endsWith("d")) {
				now = now.plusDays(Long.parseLong(s.replaceAll("[^0-9.]", "")));
			}
			
			if(s.endsWith("h")) {
				now = now.plusHours(Long.parseLong(s.replaceAll("[^0-9.]", "")));
			}
			
			if(s.endsWith("m")) {
				now = now.plusMinutes(Long.parseLong(s.replaceAll("[^0-9.]", "")));
			}
			
			if(s.endsWith("s")) {
				now = now.plusSeconds(Long.parseLong(s.replaceAll("[^0-9.]", "")));
			}
		}
		
		return now;
	}

	public void endGiveaway() {
		List<User> reactedUsers = new ArrayList<>();
		
		channel.retrieveMessageById(messageId).queue((message) -> {
			for (MessageReaction reaction : message.getReactions()) 
			{
				reactedUsers.addAll(reaction.retrieveUsers().complete());
			}
		});
		
		channel.sendMessage(messageId);
		
		Main.getInstance().getGiveaways().remove(this);
	}
}
