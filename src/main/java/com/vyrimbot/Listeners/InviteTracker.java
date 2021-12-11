package com.vyrimbot.Listeners;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;

public class InviteTracker extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();

        YamlFile lang = Main.getInstance().getLang();

        if(message.startsWith(Main.getPrefix()+"invite")) {
            String url;
            if(Main.getInstance().getDatabase().hasInvite(member.getUser())) {
                url = Main.getInstance().getDatabase().getInvite(member.getUser());
            } else {
                url = channel.createInvite().setTemporary(false).complete().getUrl();

                Main.getInstance().getDatabase().saveInvite(member.getUser(), url);
            }

            EmbedBuilder embed = EmbedUtil.getEmbed();
            embed.setTitle(lang.getString("EmbedMessages.Invite.Title"));
            embed.setColor(Color.getColor(lang.getString("EmbedMessages.Invite.Color")));

            StringBuilder description = new StringBuilder();
            for(String s : lang.getStringList("EmbedMessages.Invite.Description")) {
                s = s.replaceAll("%user%", member.getUser().getName());
                s = s.replaceAll("%user-avatar%", member.getAsMention());
                s = s.replaceAll("%link%", url);

                description.append(s).append("\n");
            }

            embed.setDescription(description.toString());
            embed.setImage(member.getAvatarUrl());
            embed.setFooter(lang.getString("EmbedMessages.Invite.Footer.Name"), lang.getString("EmbedMessages.Invite.Footer.URL"));

            channel.sendMessageEmbeds(embed.build()).queue();
        }
    }
}
