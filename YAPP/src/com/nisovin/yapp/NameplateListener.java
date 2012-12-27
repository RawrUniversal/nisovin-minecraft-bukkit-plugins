package com.nisovin.yapp;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

public class NameplateListener implements Listener {

	@EventHandler
	public void onNameplate(PlayerReceiveNameTagEvent event) {
		String name = event.getNamedPlayer().getName();
		if (name.length() <= 14) {
			User user = MainPlugin.getPlayerUser(name);
			String world = event.getPlayer().getWorld().getName();
			ChatColor color = user.getColor(world);
			if (color != null && color != ChatColor.WHITE) {
				event.setTag(color + event.getTag());
			}
		}
	}
	
}
