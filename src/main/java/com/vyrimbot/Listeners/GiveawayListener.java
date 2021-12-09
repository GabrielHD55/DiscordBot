package com.vyrimbot.Listeners;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.vyrimbot.Main;
import com.vyrimbot.Giveaways.Giveaway;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class GiveawayListener extends ListenerAdapter {

	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
		Map<String, Giveaway> giveaways = Main.getGiveawayManager().getGiveaways();
		String mID = event.getMessageId();

		if(giveaways.size() == 0 || event.getUser().isBot()) return;	
		
		if(giveaways.containsKey(mID)) {
			Giveaway g = giveaways.get(mID);
			
			g.addUser(event.getUser().getAsMention());
		}
	}
	
	@Override
	public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
		Map<String, Giveaway> giveaways = Main.getGiveawayManager().getGiveaways();
		String mID = event.getMessageId();
		
		if(giveaways.size() == 0 || event.getUser().isBot()) return;
		
		if(giveaways.containsKey(mID)) {
			Giveaway g = giveaways.get(mID);
			
			g.removeUser(event.getUser().getAsMention());
		}
	}
}
