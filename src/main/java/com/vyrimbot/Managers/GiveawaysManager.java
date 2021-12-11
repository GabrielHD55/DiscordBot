package com.vyrimbot.Managers;

import com.vyrimbot.Main;
import com.vyrimbot.Giveaways.Giveaway;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GiveawaysManager {

	@Getter private final Map<String, Giveaway> giveaways = new HashMap<>();
	@Getter private final Map<String, TimerTask> tasks = new HashMap<>();
	
	Timer timer = new Timer();
	
	public void addGiveaway(Giveaway giveaway) {
		giveaways.put(giveaway.getMessageId(), giveaway);
		
		TimerTask task = new TimerTask() {
			public void run(){
				giveaway.endGiveaway();
			}
		};
		
		timer.schedule(task, Duration.between(LocalDateTime.now(), giveaway.getExpirationDate()).toMillis());
		
		tasks.put(giveaway.getMessageId(), task);
		
		Main.getInstance().getDatabase().saveGiveaway(giveaway);
	}
	
	public void removeGiveaway(Giveaway giveaway) {
		giveaways.remove(giveaway.getMessageId(), giveaway);
		
		tasks.get(giveaway.getMessageId()).cancel();
		
		Main.getInstance().getDatabase().deleteGiveaway(giveaway);
	}
	
	public Giveaway getGiveaway(String messageId) 
	{
		return giveaways.get(messageId);
	}
}
