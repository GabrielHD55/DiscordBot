package com.vyrimbot.Listeners;

import org.jetbrains.annotations.NotNull;

import com.vyrimbot.Main;
import com.vyrimbot.Logs.Log;
import com.vyrimbot.Logs.LogType;

import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageEmbedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class LogsListener extends ListenerAdapter {

	@Override
	public void onGuildMessageEmbed(GuildMessageEmbedEvent event) {
		if(event.getChannel() == Main.getLogsManager().getChannelP() || event.getChannel() == Main.getLogsManager().getChannelR()) return;
		new Log(LogType.Root, "Embed Message", "New Message #" + event.getMessageId() + " in channel " + event.getChannel().getName());
		return;
	}
	
	@Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
		if(event.getChannel() == Main.getLogsManager().getChannelP() || event.getChannel() == Main.getLogsManager().getChannelR()) return;
		new Log(LogType.Root, event.getMessage().getContentDisplay(), "User "+ event.getAuthor().getAsTag() +" has sent a message: " + event.getMessageId() + ", in channel " + event.getChannel().getName());
		return;
	}
	
	@Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
		new Log(LogType.Root, event.getMessage().getContentDisplay(), "User "+ event.getAuthor().getAsTag() +" has updated a message: " + event.getMessageId() + ", in channel " + event.getChannel().getName());
		return;
	}
	
	@Override
    public void onGuildMessageDelete(@NotNull GuildMessageDeleteEvent event) {
		new Log(LogType.Root,"**","User has deleted a message: " + event.getMessageId() + ", in channel " + event.getChannel().getName());
		return;
	}
	
	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		new Log(LogType.Root,"User join", "User " + event.getUser().getAsTag() + " has joined!");
		return;
	}
	
	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		new Log(LogType.Root,"User leave", "User " + event.getUser().getAsTag() + " has leaved!");
		return;
	}

	@Override
	public void onGuildBan(GuildBanEvent e) {
		new Log(LogType.Punishment,"User ban", "User " + e.getUser().getAsTag() + " has been banned!");
		return;
	}
	
	@Override
	public void onGuildUnban(GuildUnbanEvent e) {
		new Log(LogType.Punishment,"User unban", "User " + e.getUser().getAsTag() + " has been unbanned!");
		return;
	}
}
