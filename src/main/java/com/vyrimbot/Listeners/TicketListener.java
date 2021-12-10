package com.vyrimbot.Listeners;

import com.vyrimbot.Main;
import com.vyrimbot.Tickets.Ticket;
import com.vyrimbot.Tickets.TicketType;
import com.vyrimbot.Utils.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.channel.text.TextChannelDeleteEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TicketListener extends ListenerAdapter {

    @Override
    public void onTextChannelDelete(TextChannelDeleteEvent event) {
        TextChannel channel = event.getChannel();
        if (Main.getTicketManager().getTicket(channel) != null) {
            Main.getTicketManager().deleteTicket(channel);
        }
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        String msg = event.getMessage().getContentRaw();

        YamlFile lang = Main.getInstance().getLang();

        if (event.getAuthor().getIdLong() == Main.getInstance().getJda().getSelfUser().getIdLong()) {
            return;
        }

        if (msg.startsWith(Main.getPrefix()+"ticketsmsg")) {
            if (member.hasPermission(Permission.MANAGE_CHANNEL)) {
                long channelID = Long.parseLong(lang.getString("Tickets.TicketsMessage.ChannelID"));

                if (channelID != 0L) {
                    TextChannel textChannel = event.getGuild().getTextChannelById(channelID);

                    long messageID = Long.parseLong(lang.getString("Tickets.TicketsMessage.MessageID"));

                    if (messageID != 0L) {
                        Message message = textChannel.getHistory().getMessageById(messageID);

                        if (message != null) message.delete().queue();
                    }
                }

                EmbedBuilder tickets = EmbedUtil.getEmbed();
                if (lang.isSet("Tickets.TicketsMessage.EmbedMessage.Title") && !lang.getString("Tickets.TicketsMessage.EmbedMessage.Title").equalsIgnoreCase(""))
                    tickets.setTitle(lang.getString("Tickets.TicketsMessage.EmbedMessage.Title"));
                if (lang.isSet("Tickets.TicketsMessage.EmbedMessage.Footer") && !lang.getString("Tickets.TicketsMessage.EmbedMessage.Footer").equalsIgnoreCase(""))
                    tickets.setFooter(lang.getString("Tickets.TicketsMessage.EmbedMessage.Footer"));
                if (lang.isSet("Tickets.TicketsMessage.EmbedMessage.ImageUrl") && !lang.getString("Tickets.TicketsMessage.EmbedMessage.ImageUrl").equalsIgnoreCase(""))
                    tickets.setImage(lang.getString("Tickets.TicketsMessage.EmbedMessage.ImageUrl"));
                if (lang.isSet("Tickets.TicketsMessage.EmbedMessage.Color") && !lang.getString("Tickets.TicketsMessage.EmbedMessage.Color").equalsIgnoreCase(""))
                    tickets.setColor(Color.getColor(lang.getString("Tickets.TicketsMessage.EmbedMessage.Color")));

                StringBuilder description = new StringBuilder();
                for (String s : lang.getStringList("Tickets.TicketsMessage.EmbedMessage.InstructionMessage")) {
                    description.append(s).append("\n");
                }
                tickets.setDescription(description.toString());

                List<Button> buttons = new ArrayList<>();
                List<ActionRow> actionRows = new ArrayList<>();

                int tick = 0;
                for(String sec : lang.getConfigurationSection("Tickets").getKeys(false)) {
                    if(lang.isSet("Tickets." + sec + ".Button")) {
                        buttons.add(Button.primary(sec.toLowerCase(), Emoji.fromUnicode(lang.getString("Tickets." + sec + ".Button.Emoji"))).withLabel(lang.getString("Tickets." + sec + ".Button.Title")));

                        tick++;
                    }

                    if(tick == 3) {
                        actionRows.add(ActionRow.of(buttons));

                        buttons.clear();
                        tick = 0;
                    }
                }

                Message message = channel.sendMessageEmbeds(tickets.build()).setActionRows(actionRows).complete();

                lang.set("Tickets.TicketsMessage.MessageID", message.getIdLong());
                lang.set("Tickets.TicketsMessage.ChannelID", channel.getIdLong());

                try {
                    lang.save();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            event.getMessage().delete().queue();
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        Member member = event.getMember();

        YamlFile lang = Main.getInstance().getLang();

        if(event.getComponent().getId().equalsIgnoreCase("closeticket")) {
            Ticket ticket = Main.getTicketManager().getTicket(event.getTextChannel());
            if(ticket != null) {
                if(!ticket.getMember().equals(member) && !hasAdminRoles(member) && !hasModRoles(member)) {
                    return;
                }
            }

            Main.getTicketManager().deleteTicket(event.getTextChannel());

            event.reply("**The ticket will be closed in 1 minute**").queue();
            return;
        }

        for(String sec : lang.getConfigurationSection("Tickets").getKeys(false)) {
            if (event.getComponent().getId().equalsIgnoreCase(sec)) {
                Ticket ticket = Main.getTicketManager().getPlayerTicket(member, TicketType.getType(sec));
                if(ticket != null) {
                    event.reply("**You already have an open ticket!** "+ticket.getTextChannel().getAsMention()).setEphemeral(true).complete();
                    return;
                }

                Main.getTicketManager().createNewTicket(member, TicketType.getType(sec));

                ticket = Main.getTicketManager().getPlayerTicket(member, TicketType.getType(sec));

                event.reply("Ticket created successfully! "+ticket.getTextChannel().getAsMention()).setEphemeral(true).queue();
                return;
            }
        }
    }

    public boolean hasAdminRoles(Member member) {
        YamlFile config = Main.getInstance().getConfig();

        for(Role role : member.getRoles()) {
            if(config.getStringList("AdminRoles").contains(String.valueOf(role.getIdLong()))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasModRoles(Member member) {
        YamlFile config = Main.getInstance().getConfig();

        for(Role role : member.getRoles()) {
            if(config.getStringList("ModRoles").contains(String.valueOf(role.getIdLong()))) {
                return true;
            }
        }
        return false;
    }
}
