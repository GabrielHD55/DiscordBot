package com.vyrimbot.Listeners;

import com.vyrimbot.Main;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class InviteTracker extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();

        if(message.startsWith(Main.getPrefix()+"invite")) {
            String url;
            if(Main.getInstance().getDatabase().hasInvite(member)) {
                url = Main.getInstance().getDatabase().getInvite(member);
            } else {
                url = channel.createInvite().setTemporary(false).complete().getUrl();
            }
        }
    }
}
