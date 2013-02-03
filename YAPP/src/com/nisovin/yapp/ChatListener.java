package com.nisovin.yapp;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

class ChatListener implements Listener {

	String chatFormat;
	
	public ChatListener(String format) {
		if (format == null || format.trim().isEmpty()) {
			this.chatFormat = "%prefix%%color%<%name%> &f%message%";
		} else {
			this.chatFormat = format;
		}		
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		User user = YAPP.getPlayerUser(event.getPlayer().getName());
		String world = event.getPlayer().getWorld().getName();
		String format = chatFormat
				.replace("%name%", "%1$s")
				.replace("%message%", "%2$s")
				.replace("%prefix%", user.getPrefix(world))
				.replace("%color%", user.getColor(world).toString())
				.replace("%rawname%", user.getName());
		format = ChatColor.translateAlternateColorCodes('&', format);
		event.setFormat(format);
	}
	
}
