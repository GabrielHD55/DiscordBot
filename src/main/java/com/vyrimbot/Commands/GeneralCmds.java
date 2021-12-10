package com.vyrimbot.Commands;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import com.vyrimbot.Giveaways.Giveaway;
import com.vyrimbot.ReactionRoles.ReactionRole;
import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;
import com.vyrimbot.Utils.ServerStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GeneralCmds extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();

        YamlFile config = Main.getInstance().getConfig();
        YamlFile lang = Main.getInstance().getLang();

        if(message.startsWith(Main.getPrefix()+"gstart")) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();

            String[] args = StringUtils.substringsBetween(message, " \"", "\"");

            EmbedBuilder embed = EmbedUtil.getEmbed(event.getAuthor());
            
            if(!event.getMember().getRoles().contains(event.getGuild().getRoleById(Long.parseLong(config.getString("GiveawayRole"))))) {
            	return;
            }
            if(args == null || args.length < 5) {
                event.getChannel().sendMessage("To create a giveaway, type "+Main.getPrefix()+"gstart [\"title\"] [\"description\"] [\"prize\"] [\"winners\"] [\"expiration_date\"]\n 1 - 15 winners.\n date format example: 1w 2d 3h 4m 5s").queue();
                return;
            }

            embed.setColor(Color.getColor(lang.getString("EmbedMessages.Giveaway.Color", "BLUE")));
            embed.setTitle(args[0]);
            embed.setDescription(args[1]);

            embed.addField(lang.getString("EmbedMessages.Giveaway.Options.Winner-Format").replace("%winner-amount%", args[3]), "", false);

            embed.addField(lang.getString("EmbedMessages.Giveaway.Options.Expiration-Date").replace("%expiration-date%", Giveaway.getFormattedExpirationDate(args[4])), "", false);

            embed.setFooter(lang.getString("Giveaway.Footer.Name"), lang.getString("Giveaway.Footer.URL"));

            Message m = event.getChannel().sendMessageEmbeds(embed.build()).complete();
            m.addReaction(Emoji.fromUnicode(lang.getString("EmbedMessages.Giveaway.Options.Reaction-Emote")).getName()).queue();
            
            Giveaway giveaway = new Giveaway(m.getId(),event.getChannel(), args);


        }
        
        if(message.startsWith(Main.getPrefix()+"reactroles")) {
        	event.getChannel().deleteMessageById(event.getMessageId()).queue();
        	
        	String[] args = StringUtils.substringsBetween(message, "'", "'");
        	String d = StringUtils.substringBetween(message, "\"","\"");
        	
        	if(args == null || args.length == 0 || d == null) {
                event.getChannel().sendMessage("To create a Reaction Role message, type "+Main.getPrefix()+ "reactroles [\"description\"] ['emoji1 role1'] ['emoji2 role2'] ... ").queue();
                return;
            }
        	
        	List<String> emojies = new ArrayList<>();
        	List<Role> roles = new ArrayList<>();
        	
        	event.getChannel().sendMessage(d).queue(current -> {
        		
        		for(String s : args) {
            		String[] split = s.split(" ");
            		
            		emojies.add(split[0]);
            		roles.add(event.getGuild().getRoleById(split[1]));
            		
            		current.addReaction(split[0]).queue();
            	}
        		
        		ReactionRole rr = new ReactionRole(current.getId(), roles, emojies);
        		
        	});
        	return;
        }
        
        if(message.startsWith(Main.getPrefix()+"glist")) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();

        	Map<String, Giveaway> giveaways = Main.getGiveawayManager().getGiveaways();
        	
        	if(giveaways.size() == 0) {
        		event.getChannel().sendMessage(lang.getString("GiveawayNoExist")).queue();
        		return;
        	}
        	
        	for(Giveaway g : giveaways.values()) {
        		String[] args = g.getArgs();
        		event.getChannel().sendMessage(lang.getString("GiveawayList").replace("%id%", g.getMessageId()).replace("%title%", args[0]).replace("%prize%", args[2]).replace("%winners%", args[3]).replace("%expiration-date%", Giveaway.getFormattedExpirationDate(args[4]))).queue();
        	}
        	return;
        }
        
        if(message.startsWith(Main.getPrefix()+"gend")) {
        	event.getChannel().deleteMessageById(event.getMessageId()).queue();
        	
        	String[] args = message.split(" ");
        	
        	if(!event.getMember().getRoles().contains(event.getGuild().getRoleById(Long.parseLong(config.getString("GiveawayRole"))))) {
            	return;
            }
        	
        	if(args.length == 0) {
        		event.getChannel().sendMessage("To ends a giveaway, type "+Main.getPrefix()+"gend [messageId]").queue();
        		return;
        	}
        	
        	Giveaway g = Main.getGiveawayManager().getGiveaway(args[1]);
        	
        	if(g == null) {
        		event.getChannel().sendMessage("Invalid messageID, type "+Main.getPrefix()+"glist to see current giveaways").queue();
        		return;
        	}
        	
        	g.endGiveaway();
        }

        if(message.startsWith(Main.getPrefix()+"poll")) {
            event.getChannel().deleteMessageById(event.getMessageId()).queue();

            String[] args = StringUtils.substringsBetween(message, " \"", "\"");

            String[] deffaultReactions = new String[] {"1️⃣","2️⃣","3️⃣","4️⃣","5️⃣","6️⃣","7️⃣","8️⃣","9️⃣","🔟"};

            EmbedBuilder embed = EmbedUtil.getEmbed(event.getAuthor());

            if(args == null || args.length < 2) {
                event.getChannel().sendMessage("To create a poll, type "+Main.getPrefix()+ "poll [\"title\"] [\"description\"] {'option1'} {'option2'} ... \n 9 options maximum.").queue();
                return;
            }
            embed.setColor(Color.getColor(lang.getString("EmbedMessages.Poll.Color", "BLUE")));
            embed.setTitle(args[0]);
            embed.setDescription(args[1]);

            String[] options = StringUtils.substringsBetween(message, " '", "'");


            int e = 0;
            if(options != null) {
                for(int i = 0; i < options.length; i++) {
                    String s = options[i];

                    if(s.split(":").length > 0) {
                        embed.addField("", s, false);
                    }
                    else {
                        embed.addField("", deffaultReactions[i] + " " + s, false);
                    }
                    e++;
                }
            }

            embed.setFooter(lang.getString("EmbedMessages.Poll.Footer.Name"), lang.getString("EmbedMessages.Poll.Footer.URL"));

            Message m = event.getChannel().sendMessageEmbeds(embed.build()).complete();

            if(e == 0) {
                m.addReaction(Emoji.fromUnicode(lang.getString("EmbedMessages.Poll.Options.Emote_Y")).getName()).queue();
                m.addReaction(Emoji.fromUnicode(lang.getString("EmbedMessages.Poll.Options.Emote_N")).getName()).queue();
                return;
            }

            for(int i = 0; i < e; i++) {
                if(i > deffaultReactions.length) break;

                if(EmojiManager.containsEmoji(options[i])) {
                    m.addReaction((String) EmojiParser.extractEmojis(options[i]).toArray()[0]).queue();
                } else {
                    m.addReaction(deffaultReactions[i]).queue();
                }
            }
            return;
        }

        if(message.startsWith(Main.getPrefix()+"status")) {
            EmbedBuilder embed = EmbedUtil.getEmbed(event.getAuthor());
            String serverIP = config.getString("Settings.ServerIP");

            if(ServerStatus.checkOnline(config.getString("Settings.ServerIP"))) {
                embed.setColor(Color.getColor(lang.getString("EmbedMessages.ServerStatus.Online.Color", "GREEN")));
                embed.setTitle(lang.getString("EmbedMessages.ServerStatus.Online.Title"));

                StringBuilder description = new StringBuilder();

                for(String s : lang.getStringList("EmbedMessages.ServerStatus.Online.Description")) {
                    s = s.replaceAll("%players-online%", ServerStatus.checkCount(serverIP));
                    s = s.replaceAll("%is-online%", (ServerStatus.checkOnline(serverIP) ? "&aONLINE" : "&cOFFLINE"));
                    s = s.replaceAll("%max-players%", ServerStatus.checkMaxOnline(serverIP));

                    description.append(s).append("\n");
                }

                embed.setDescription(description.toString());
                embed.setFooter(lang.getString("EmbedMessages.ServerStatus.Online.Footer.Name"), lang.getString("EmbedMessages.ServerStatus.Online.Footer.URL"));
            } else {
                embed.setColor(Color.getColor(lang.getString("EmbedMessages.ServerStatus.Offline.Color", "RED")));
                embed.setTitle(lang.getString("EmbedMessages.ServerStatus.Offline.Title"));

                StringBuilder description = new StringBuilder();
                for(String s : lang.getStringList("EmbedMessages.ServerStatus.Offline.Description")) {
                    s = s.replaceAll("%is-online%", (ServerStatus.checkOnline(serverIP) ? "&aONLINE" : "&cOFFLINE"));
                    s = s.replaceAll("%max-players%", ServerStatus.checkMaxOnline(serverIP));

                    description.append(s).append("\n");
                }

                embed.setDescription(description.toString());
                embed.setFooter(lang.getString("EmbedMessages.ServerStatus.Offline.Footer.Name"), lang.getString("EmbedMessages.ServerStatus.Offline.Footer.URL"));
            }

            channel.sendMessageEmbeds(embed.build()).queue();
            return;
        }

        if(message.startsWith(Main.getPrefix()+"whois")) {
            String[] args = message.split(" ");

            Member target;

            if(args.length == 1) {
                target = member;
            } else if(args.length == 2) {
                if(!event.getMessage().getMentionedMembers(event.getGuild()).isEmpty()) {
                    target = event.getMessage().getMentionedMembers(event.getGuild()).get(0);
                } else {

                    String userName = args[1];

                    target = event.getGuild().getMembersByName(userName, true).get(0);
                }
            } else {
                event.getChannel().sendMessage("To get a users info, type "+Main.getPrefix()+"whois [name]").queue();
                return;
            }

            EmbedBuilder embed = EmbedUtil.getEmbed(target.getUser());
            embed.setColor(Color.getColor(lang.getString("EmbedMessages.Whois.Color", "BLUE")));
            embed.setTitle(lang.getString("EmbedMessages.Whois.Title").replace("%user%", target.getUser().getName()));

            StringBuilder roles = new StringBuilder();
            for(Role role : target.getRoles()) {
                roles.append(role.getAsMention()).append(" ");
            }

            StringBuilder description = new StringBuilder();
            for(String s : lang.getStringList("EmbedMessages.Whois.Description")) {
                s = s.replaceAll("%user%", target.getUser().getName());
                s = s.replaceAll("%status%", target.getOnlineStatus().toString());
                s = s.replaceAll("%user-avatar%", target.getAsMention());
                s = s.replaceAll("%roles%", roles.toString());
                s = s.replaceAll("%join-date%", target.getTimeJoined().toString());
                s = s.replaceAll("%creation-date%", target.getTimeCreated().toString());

                description.append(s).append("\n");
            }

            embed.setDescription(description.toString());
            embed.setImage(target.getAvatarUrl());
            embed.setFooter(lang.getString("EmbedMessages.Whois.Footer.Name"), lang.getString("EmbedMessages.Whois.Footer.URL"));

            channel.sendMessageEmbeds(embed.build()).queue();
            return;
        }

        if(message.startsWith(Main.getPrefix()+"setip")) {
            if(!member.hasPermission(Permission.ADMINISTRATOR)) {
                return;
            }

            String[] args = message.split(" ");

            if(args.length != 2) {
                channel.sendMessage("Use: "+Main.getPrefix()+"setip <serverip>");
                return;
            }

            String serverIP = args[1];

            new Thread(() -> {
                config.set("Settings.ServerIP", serverIP);
                try {
                    config.save();

                    channel.sendMessage("**IP updated correctly**").queue(msg -> msg.delete().queueAfter(5L, TimeUnit.SECONDS));
                } catch (IOException e) {
                    channel.sendMessage("**An error occurred when trying to update the IP, please check the console!**").queue(msg -> msg.delete().queueAfter(10L, TimeUnit.SECONDS));

                    e.printStackTrace();
                }
            }).start();
        }
    }
}
