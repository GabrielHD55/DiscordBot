package com.vyrimbot.Giveaways;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.simpleyaml.configuration.file.YamlFile;

import com.vyrimbot.Main;

import lombok.Getter;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;

public class Giveaway {

	@Getter private final String messageId;
	@Getter private final short winnersAmount;
	@Getter private final LocalDateTime expirationDate;
	@Getter private final TextChannel channel;
	@Getter private final String[] args;
	@Getter private final List<String> users;
	
	public Giveaway(String messageId, TextChannel channel, String[] args) {
		this.messageId = messageId;
		this.expirationDate = getExpirationDate(args[4]);
		this.winnersAmount = Short.parseShort(args[3]);
		this.channel = channel;
		this.args = args;
		users = new ArrayList<>();
		
		Main.getGiveawayManager().addGiveaway(this);
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
		YamlFile lang = Main.getInstance().getLang();			
		
		StringBuilder winners = new StringBuilder();
		
		for(int i = 0; i < winnersAmount; i++) {
			String[] users = this.users.toArray(new String[this.users.toArray().length]);
			
			if(users == null || users.length == 0) {
				channel.sendMessage(lang.getString("GiveawayNoWinners")).queue();
				Main.getGiveawayManager().removeGiveaway(this);
				return;
			}
			
			int r = new Random().nextInt(users.length);
			
			winners.append(users[r]).append(" ");
			this.users.remove(r);
		}
		channel.sendMessage(lang.getString("GiveawayWinners").replace("%winners%", winners.toString()).replace("%giveaway-title%", args[0])).queue();
		
		Main.getGiveawayManager().removeGiveaway(this);
	}

	public static String getFormattedExpirationDate(String timeToAdd) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy HH:mm:ss");
		return getExpirationDate(timeToAdd).format(formatter);
	}
	
	public void addUser(String u) {
		users.add(u);
	}
	
	public void removeUser(String u) {
		users.remove(u);
	}
}
