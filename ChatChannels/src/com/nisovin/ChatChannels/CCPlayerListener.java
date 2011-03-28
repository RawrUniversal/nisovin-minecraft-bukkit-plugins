package com.nisovin.ChatChannels;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

public class CCPlayerListener extends PlayerListener {

	private ChatChannels plugin;
	
	public CCPlayerListener(ChatChannels plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player p = event.getPlayer();
		if (!plugin.activeChannels.containsKey(p.getName())) {
			Channel global = plugin.getChannel("Global");
			global.join(p);
			plugin.activeChannels.put(p.getName(), global);
			p.sendMessage("You have joined the '" + global.getColoredName() + "' channel.");
			
			if (p.isOp()) {
				Channel admin = plugin.getChannel("Admin");
				admin.forceJoin(p);
				p.sendMessage("You have joined the '" + admin.getColoredName() + "' channel.");
			}
		}
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		Player p = event.getPlayer();
		Channel channel = plugin.activeChannels.get(p.getName());
		if (channel != null) {
			channel.sendMessage(p, event.getMessage());
		} else {
			p.sendMessage("You are not in a channel.");
		}
		event.setCancelled(true);
	}
	
	
	
}
