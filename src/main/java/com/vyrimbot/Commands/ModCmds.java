package com.vyrimbot.Commands;

import com.vyrimbot.Main;
import com.vyrimbot.Utils.EmbedUtil;
import com.vyrimbot.Utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.simpleyaml.configuration.file.YamlFile;

import java.awt.*;

public class ModCmds extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        String message = event.getMessage().getContentRaw();

        YamlFile config = Main.getInstance().getConfig();
        YamlFile lang = Main.getInstance().getLang();

        if (message.startsWith(Main.getPrefix() + "ban")) {
            if(!hasAdminRoles(member)) {
                return;
            }

            String[] args = message.split(" ");
            if(args.length <= 1 || args.length >= 4) {
                channel.sendMessage("You are missing arguments, please use: "+Main.getPrefix()+"ban [Name] [Reason]").queue();
                return;
            }

            String reason = lang.getString("BanDefaultReason", "Banned for bad behavior");
            if(args.length >= 3) {
                StringBuilder stringBuilder = new StringBuilder(args[2]);

                for(int i = 3; i < args.length; i++) {
                    stringBuilder.append(args[i]);
                }

                reason = stringBuilder.toString();
            }

            Member target = null;
            if(!event.getMessage().getMentionedMembers(event.getGuild()).isEmpty()) {
                target = event.getMessage().getMentionedMembers(event.getGuild()).get(0);
            } else if(Utils.isLong(args[1])) {
                target = event.getGuild().getMemberById(Long.parseLong(args[1]));
            } else if(!event.getGuild().getMembersByName(args[1], true).isEmpty()) {
                target = event.getGuild().getMembersByName(args[1], true).get(0);
            }

            if(target != null && !target.getUser().isBot()) {
                if(hasAdminRoles(target)) {
                    Message msg = channel.sendMessage("**This member could not be banned**").complete();

                    new Thread(() -> {
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        msg.delete().queue();
                    }).start();
                    return;
                }

                EmbedBuilder embed = EmbedUtil.getEmbed(target.getUser());
                embed.setTitle(lang.getString("EmbedMessages.Ban.Title").replace("%user%", target.getUser().getName()));
                embed.setColor(Color.getColor(lang.getString("EmbedMessages.Ban.Color", "RED")));

                StringBuilder description = new StringBuilder();
                for(String s : lang.getStringList("EmbedMessages.Ban.Description")) {
                    s = s.replaceAll("%reason%", reason);
                    s = s.replaceAll("%user%", target.getUser().getName());
                    s = s.replaceAll("%user-avatar%", target.getAsMention());
                    s = s.replaceAll("%admin%", member.getUser().getName());
                    s = s.replaceAll("%admin-avatar%", member.getAsMention());

                    description.append(s).append("\n");
                }
                embed.setDescription(description.toString());
                embed.setImage(target.getAvatarUrl());
                embed.setFooter(lang.getString("EmbedMessages.Ban.Footer.Name"), lang.getString("EmbedMessages.Ban.Footer.URL"));

                Member finalTarget = target;
                String finalReason = reason;

                event.getGuild().ban(target, 1, reason).queue(success -> {
                    Main.getInstance().getDatabase().banUser(finalTarget, member, finalReason);
                    
                    channel.sendMessageEmbeds(embed.build()).queue();
                }, failure -> {
                    Message msg = channel.sendMessage("**This member could not be banned**").complete();

                    new Thread(() -> {
                        try {
                            Thread.sleep(5000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        msg.delete().queue();
                    }).start();
                });
            }
            return;
        }

        if(message.startsWith(Main.getPrefix() + "unban")) {
            if(!hasAdminRoles(member)) {
                return;
            }

            String[] args = message.split(" ");
            if(args.length != 2) {
                channel.sendMessage("You are missing arguments, please use: "+Main.getPrefix()+" unban [User ID]").queue();
                return;
            }

            User target = Main.getInstance().getJda().getUserById(args[1]);
            if(target != null) {
                String unbanMessage = lang.getString("UnbannedMessage");
                unbanMessage = unbanMessage.replaceAll("%user%", target.getName());
                unbanMessage = unbanMessage.replaceAll("%user-avatar%", target.getAsMention());
                unbanMessage = unbanMessage.replaceAll("%admin%", member.getUser().getName());
                unbanMessage = unbanMessage.replaceAll("%admin-avatar%", member.getAsMention());
                String finalUnbanMessage = unbanMessage;

                event.getGuild().unban(target).queue(success -> channel.sendMessage(finalUnbanMessage).queue(), failure -> {
                    Message msg = channel.sendMessage("**This user is not banned**").complete();

                    new Thread(() -> {
                        try {
                            Thread.sleep(10000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        msg.delete().queue();
                    }).start();
                });
            }
            return;
        }

        if(message.startsWith(Main.getPrefix() + "mute")) {
            if(!hasAdminRoles(member) && !hasModRoles(member)) {
                return;
            }

            String[] args = message.split(" ");
            if(args.length <= 1) {
                channel.sendMessage("You are missing arguments, please use: "+Main.getPrefix()+"mute [Name] [Time] [Reason]").queue();
                return;
            }

            String reason = lang.getString("MuteDefaultReason");
            if(args.length >= 4) {
                StringBuilder stringBuilder = new StringBuilder(args[2]);

                for(int i = 3; i < args.length; i++) {
                    stringBuilder.append(args[i]);
                }

                reason = stringBuilder.toString();
            }

            Member target = null;
            if(!event.getMessage().getMentionedMembers(event.getGuild()).isEmpty()) {
                target = event.getMessage().getMentionedMembers(event.getGuild()).get(0);
            } else if(Utils.isLong(args[1])) {
                target = event.getGuild().getMemberById(Long.parseLong(args[1]));
            } else if(!event.getGuild().getMembersByName(args[1], true).isEmpty()) {
                target = event.getGuild().getMembersByName(args[1], true).get(0);
            }

            if(target != null && !target.getUser().isBot()) {
                if(isMuted(member)) {
                    return;
                }
                EmbedBuilder embed = EmbedUtil.getEmbed(target.getUser());
                embed.setTitle(lang.getString("EmbedMessages.Mute.Title").replace("%user%", target.getUser().getName()));
                embed.setColor(Color.getColor(lang.getString("EmbedMessages.Mute.Color", "RED")));

                StringBuilder description = new StringBuilder();
                for(String s : lang.getStringList("EmbedMessages.Mute.Description")) {
                    s = s.replaceAll("%reason%", reason);
                    s = s.replaceAll("%user%", target.getUser().getName());
                    s = s.replaceAll("%user-avatar%", target.getAsMention());
                    s = s.replaceAll("%admin%", member.getUser().getName());
                    s = s.replaceAll("%admin-avatar%", member.getAsMention());

                    description.append(s).append("\n");
                }
                embed.setDescription(description.toString());
                embed.setImage(target.getAvatarUrl());
                embed.setFooter(lang.getString("EmbedMessages.Mute.Footer.Name"), lang.getString("EmbedMessages.Mute.Footer.URL"));

                channel.sendMessageEmbeds(embed.build()).queue();

                Role role = event.getJDA().getRoleById(Long.parseLong(config.getString("MuteRole")));
                if(role != null) {
                    event.getGuild().addRoleToMember(target, role).queue();
                }
            }
            return;
        }

        if(message.startsWith(Main.getPrefix() + "unmute")) {
            if(!hasAdminRoles(member) && !hasModRoles(member)) {
                return;
            }

            String[] args = message.split(" ");
            if(args.length != 2) {
                channel.sendMessage("You are missing arguments, please use: "+Main.getPrefix()+"mute [Name]").queue();
                return;
            }

            Member target = null;
            if(!event.getMessage().getMentionedMembers(event.getGuild()).isEmpty()) {
                target = event.getMessage().getMentionedMembers(event.getGuild()).get(0);
            } else if(Utils.isLong(args[1])) {
                target = event.getGuild().getMemberById(Long.parseLong(args[1]));
            } else if(!event.getGuild().getMembersByName(args[1], true).isEmpty()) {
                target = event.getGuild().getMembersByName(args[1], true).get(0);
            }

            if(target == null) {
                if (isMuted(target)) {
                    Role role = event.getJDA().getRoleById(Long.parseLong(config.getString("MuteRole")));

                    event.getGuild().removeRoleFromMember(member, role).queue();

                    String unbanMessage = lang.getString("UnbannedMessage");
                    unbanMessage = unbanMessage.replaceAll("%user%", target.getUser().getName());
                    unbanMessage = unbanMessage.replaceAll("%user-avatar%", target.getAsMention());
                    unbanMessage = unbanMessage.replaceAll("%admin%", member.getUser().getName());
                    unbanMessage = unbanMessage.replaceAll("%admin-avatar%", member.getAsMention());

                    channel.sendMessage(unbanMessage).queue();
                } else {
                    channel.sendMessage("**This user isn't muted**").queue();
                }
            }
            return;
        }

        if(message.startsWith(Main.getPrefix() + "kick")) {
            if(!hasAdminRoles(member) && !hasModRoles(member)) {
                return;
            }

            String[] args = message.split(" ");
            if(args.length <= 1) {
                channel.sendMessage("You are missing arguments, please use: "+Main.getPrefix()+"kick [Name] [Reason]").queue();
                return;
            }

            String reason = lang.getString("KickDefaultReason");
            if(args.length >= 4) {
                StringBuilder stringBuilder = new StringBuilder(args[2]);

                for(int i = 3; i < args.length; i++) {
                    stringBuilder.append(args[i]);
                }

                reason = stringBuilder.toString();
            }

            Member target = null;
            if(!event.getMessage().getMentionedMembers(event.getGuild()).isEmpty()) {
                target = event.getMessage().getMentionedMembers(event.getGuild()).get(0);
            } else if(Utils.isLong(args[1])) {
                target = event.getGuild().getMemberById(Long.parseLong(args[1]));
            } else if(!event.getGuild().getMembersByName(args[1], true).isEmpty()) {
                target = event.getGuild().getMembersByName(args[1], true).get(0);
            }

            if(target != null) {
                if(!target.getUser().isBot()) {
                    EmbedBuilder embed = EmbedUtil.getEmbed(target.getUser());
                    embed.setTitle(lang.getString("EmbedMessages.Kick.Title").replace("%user%", target.getUser().getName()));
                    embed.setColor(Color.getColor(lang.getString("EmbedMessages.Kick.Color", "RED")));

                    StringBuilder description = new StringBuilder();
                    for(String s : lang.getStringList("EmbedMessages.Kick.Description")) {
                        s = s.replaceAll("%reason%", reason);
                        s = s.replaceAll("%user%", target.getUser().getName());
                        s = s.replaceAll("%user-avatar%", target.getAsMention());
                        s = s.replaceAll("%admin%", member.getUser().getName());
                        s = s.replaceAll("%admin-avatar%", member.getAsMention());

                        description.append(s).append("\n");
                    }
                    embed.setDescription(description.toString());
                    embed.setImage(target.getAvatarUrl());
                    embed.setFooter(lang.getString("EmbedMessages.Kick.Footer.Name"), lang.getString("EmbedMessages.Kick.Footer.URL"));

                    event.getGuild().kick(target, reason).queue(success -> channel.sendMessageEmbeds(embed.build()).queue(), failure -> {
                        Message msg = channel.sendMessage("**This member could not be kicked**").complete();

                        new Thread(() -> {
                            try {
                                Thread.sleep(5000L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            msg.delete().queue();
                        }).start();
                    });
                }
            } else {
                Message msg = channel.sendMessage("**This member does not exist!**").complete();

                new Thread(() -> {
                    try {
                        Thread.sleep(5000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    msg.delete().queue();
                }).start();
            }
        }
    }

    public boolean isMuted(Member member) {
        YamlFile config = Main.getInstance().getConfig();

        for(Role role : member.getRoles()) {
            if(role.getIdLong() == Long.parseLong(config.getString("MuteRole"))) {
                return true;
            }
        }
        return false;
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
