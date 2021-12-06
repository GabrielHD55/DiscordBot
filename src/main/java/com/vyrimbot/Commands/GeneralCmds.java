package com.vyrimbot.Commands;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;
import com.vyrimbot.Utils.ServerStatus;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;
import java.io.File;

public class GeneralCmds extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        String message = event.getMessage().getContentRaw();

        YamlFile config = Main.getInstance().getConfig();
        YamlFile lang = Main.getInstance().getLang();

        if(message.startsWith(Main.getPrefix()+"status")) {
            EmbedBuilder embed = EmbedUtil.getEmbed(event.getAuthor());

            if(ServerStatus.checkOnline(config.getString("Settings.ServerIP"))) {
                embed.setColor(Color.getColor(lang.getString("ServerStatus.Online.Color", "GREEN")));
                embed.setTitle(lang.getString("ServerStatus.Online.Title"));

                StringBuilder description = new StringBuilder();
                for(String s : lang.getStringList("ServerStatus.Online.Description")) {
                    description.append(s).append("\n");
                }

                embed.setDescription(description.toString());
                embed.setFooter(lang.getString("ServerStatus.Online.Footer.Name"), lang.getString("ServerStatus.Online.Footer.URL"));
            } else {
                embed.setColor(Color.getColor(lang.getString("ServerStatus.Offline.Color", "RED")));
                embed.setTitle(lang.getString("ServerStatus.Offline.Title"));

                StringBuilder description = new StringBuilder();
                for(String s : lang.getStringList("ServerStatus.Offline.Description")) {
                    description.append(s).append("\n");
                }

                embed.setDescription(description.toString());
                embed.setFooter(lang.getString("ServerStatus.Offline.Footer.Name"), lang.getString("ServerStatus.Offline.Footer.URL"));
            }

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
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
            embed.setColor(Color.getColor(lang.getString("Whois.Color", "BLUE")));
            embed.setTitle(lang.getString("Whois.Title").replace("%user%", target.getUser().getName()));

            StringBuilder roles = new StringBuilder();
            for(Role role : target.getRoles()) {
                roles.append(role.getAsMention()).append(" ");
            }

            StringBuilder description = new StringBuilder();
            for(String s : lang.getStringList("Whois.Description")) {
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
            embed.setFooter(lang.getString("Whois.Footer.Name"), lang.getString("Whois.Footer.URL"));

            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        }
    }
}
