package com.vyrimbot.Tickets;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;

public class Ticket {

    private final Member member;
    private final TicketType type;
    private TextChannel textChannel;

    public Ticket(Member member, TicketType type) {
        this.member = member;
        this.type = type;

        this.textChannel = null;
    }

    public void createNewTicket() {
        if (this.textChannel == null) {
            YamlFile lang = Main.getInstance().getLang();

            this.textChannel = Main.getInstance().getJda().getCategoryById(lang.getLong("Tickets." + type.name() + ".CategoryID")).createTextChannel(lang.getString("Settings." + type.name() + ".ChannelPrefix") + member.getUser().getName()).complete();

            if (this.textChannel.getPermissionOverride(member) == null) {
                this.textChannel.createPermissionOverride(member).setAllow(
                        Permission.VIEW_CHANNEL,
                        Permission.MESSAGE_WRITE,
                        Permission.MESSAGE_READ,
                        Permission.MESSAGE_HISTORY,
                        Permission.MESSAGE_EMBED_LINKS,
                        Permission.MESSAGE_ATTACH_FILES,
                        Permission.MESSAGE_ADD_REACTION,
                        Permission.MESSAGE_EXT_EMOJI
                ).queue();
            }

            EmbedBuilder instrutions = EmbedUtil.getEmbed(member.getUser());
            if (lang.isSet("Tickets." + type.name() + ".EmbedMessage.Title") && !lang.getString("Tickets." + type.name() + ".EmbedMessage.Title").equalsIgnoreCase(""))
                instrutions.setTitle(lang.getString("Tickets." + type.name() + ".EmbedMessage.Title"));
            if (lang.isSet("Tickets." + type.name() + ".EmbedMessage.Footer") && !lang.getString("Tickets." + type.name() + ".EmbedMessage.Footer").equalsIgnoreCase(""))
                instrutions.setFooter(lang.getString("Tickets." + type.name() + ".EmbedMessage.Footer"));
            if (lang.isSet("Tickets." + type.name() + ".EmbedMessage.ImageUrl") && !lang.getString("Tickets." + type.name() + ".EmbedMessage.ImageUrl").equalsIgnoreCase(""))
                instrutions.setImage(lang.getString("Tickets." + type.name() + ".EmbedMessage.ImageUrl"));
            if (lang.isSet("Tickets." + type.name() + ".EmbedMessage.Color") && !lang.getString("Tickets." + type.name() + ".EmbedMessage.Color").equalsIgnoreCase(""))
                instrutions.setColor(Color.getColor(lang.getString("Tickets." + type.name() + ".EmbedMessage.Color")));
            StringBuilder description = new StringBuilder();
            for (String s : lang.getStringList("Tickets." + type.name() + ".EmbedMessage.InstructionMessage")) {
                description.append(s).append("\n");
            }
            instrutions.setDescription(description.toString());

            this.textChannel.sendMessage(member.getAsMention()).queue();
            this.textChannel.sendMessage(instrutions.build()).complete().addReaction("\uD83D\uDD12").queue();

        }
    }

    public Member getMember() {
        return member;
    }

    public TextChannel getTextChannel() {
        return textChannel;
    }

    public void setTextChannel(TextChannel textChannel) {
        this.textChannel = textChannel;
    }

    public TicketType getType() {
        return type;
    }
}
