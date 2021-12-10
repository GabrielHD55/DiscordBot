package com.vyrimbot.Managers;

import com.vyrimbot.Main;
import com.vyrimbot.ReactionRoles.ReactionRole;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.*;

public class ReactionRoleManager {

	@Getter private final Map<String, ReactionRole> reactionRoles = new HashMap<>();
	
	public ReactionRoleManager() {
		YamlFile config = Main.getInstance().getConfig();
		
		Set<String> section = config.getConfigurationSection("ReactionRoles.Messages").getKeys(false);
		
		TextChannel channel = Main.getInstance().getJda().getTextChannelById(config.getString("ReactionRoles.ChannelID"));
		
		for(String sec : section) {
			String d = config.getString("ReactionRoles.Messages." + sec + ".Description");
			
			List<String> emojies = new ArrayList<>();
        	List<Role> roles = new ArrayList<>();
			
			channel.sendMessage(d).queue(m -> {
				ReactionRole rr = new ReactionRole(m.getId(), roles, emojies);
				
				for(String s : config.getStringList("ReactionRoles.Messages." + sec + ".ReactionRoles")) {
					String[] split = s.split(" ");
	        		
	        		emojies.add(Emoji.fromUnicode(split[0]).getName());
	        		roles.add(channel.getGuild().getRoleById(split[1]));
	        		
	        		m.addReaction(Emoji.fromUnicode(split[0]).getName()).queue();
				}
			});
		}
	}

	public void addReactionRole(String messageId, ReactionRole rr) {
		reactionRoles.put(messageId, rr);
	}
	
	public ReactionRole getReactionRole(String messageId) {
		return reactionRoles.get(messageId);
	}
}
