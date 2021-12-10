package com.vyrimbot.Listeners;

import org.jetbrains.annotations.NotNull;

import com.vyrimbot.Main;
import com.vyrimbot.ReactionRoles.ReactionRole;

import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionRoleListener extends ListenerAdapter{
	
	@Override
	public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {

		String mId = event.getMessageId();
		String emoji = event.getReactionEmote().getEmoji();
		
		if(!Main.getRrManager().getReactionRoles().containsKey(mId)) {
			return;
		}
		
		ReactionRole rr = Main.getRrManager().getReactionRoles().get(mId);
		
		event.getGuild().addRoleToMember(event.getUserId(), rr.getRoleFromEmoji(emoji)).queue();
		
		return;
	}
	
	@Override
	public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {

		String mId = event.getMessageId();
		String emoji = event.getReactionEmote().getEmoji();
		
		if(!Main.getRrManager().getReactionRoles().containsKey(mId)) {
			return;
		}
		
		ReactionRole rr = Main.getRrManager().getReactionRoles().get(mId);
		
		event.getGuild().removeRoleFromMember(event.getUserId(), rr.getRoleFromEmoji(emoji)).queue();
		
		return;
	}

}
