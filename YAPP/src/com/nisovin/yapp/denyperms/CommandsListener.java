package com.nisovin.yapp.denyperms;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandsListener implements Listener {

	@EventHandler(priority=EventPriority.LOW)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (event.getPlayer().hasPermission("yapp.deny.commands")) {
			event.setCancelled(true);
		}
	}
	
}
