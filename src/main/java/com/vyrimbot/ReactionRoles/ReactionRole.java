package com.vyrimbot.ReactionRoles;


import java.util.List;

import com.vyrimbot.Main;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;

public class ReactionRole {
	@Getter List<Role> roles;
	@Getter List<String> emojies;
	
	public ReactionRole(String messageID, List<Role> roles, List<String> emojies) {
		this.roles = roles;
		this.emojies = emojies;
		
		Main.getRrManager().addReactionRole(messageID, this);
	}
	
	public Role getRoleFromEmoji(String emoji) {
		return roles.get(emojies.indexOf(emoji));
	}
}
