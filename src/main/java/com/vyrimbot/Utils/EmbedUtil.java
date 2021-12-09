package com.vyrimbot.Utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;
import java.text.MessageFormat;
import java.time.LocalDateTime;

public class EmbedUtil {

    public static EmbedBuilder getEmbed(User user) {
        return getEmbed().setFooter(MessageFormat.format("Requested by: {0}", getTag(user)), user.getEffectiveAvatarUrl()).setTimestamp(LocalDateTime.now());
    }

    public static EmbedBuilder getEmbed() {
        return new EmbedBuilder().setTimestamp(LocalDateTime.now()).setColor(new Color(54, 57, 63));
    }

    public static String getTag(User user) {
        return user.getName() + "#" + user.getDiscriminator();
    }
    
    public static String getSuggestionTitle(String author, String id) {
        return  "#" + id + " " + author;
    }
    
    public static String getSuggestionAuthor(String title) {
    	String[] args = title.split(" ");
    	return args[1];
    }
}