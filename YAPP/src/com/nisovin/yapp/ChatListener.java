package com.nisovin.yapp;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		User user = MainPlugin.getPlayerUser(event.getPlayer().getName());
		String world = event.getPlayer().getWorld().getName();
		event.setFormat(user.getPrefix(world) + "<" + user.getColor(world) + "%1$s" + ChatColor.WHITE + "> %2$s");
	}
	
}
