package com.nisovin.yapp.denyperms;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class DenyChatListener implements Listener {
		
	private Set<String> deniedChat = Collections.synchronizedSet(new HashSet<String>());
	
	public void setDeniedChat(Player player, boolean denied) {
		if (denied) {
			deniedChat.add(player.getName().toLowerCase());
		} else {
			deniedChat.remove(player.getName().toLowerCase());
		}
	}
	
	public void remove(Player player) {
		deniedChat.remove(player.getName().toLowerCase());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(final AsyncPlayerChatEvent event) {
		if (deniedChat.contains(event.getPlayer().getName().toLowerCase())) {
			event.setCancelled(true);
		}
	}

}
