package com.nisovin.ChatChannels;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;

public class CCPlayerListener extends PlayerListener {

	private ChatChannels plugin;
	
	public CCPlayerListener(ChatChannels plugin) {
		this.plugin = plugin;
				
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, this, Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, this, Priority.High, plugin);
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (!ChatChannels.activeChannels.containsKey(p.getName())) {
			Channel global = plugin.getChannel("Global");
			global.join(p);
			ChatChannels.activeChannels.put(p.getName(), global);
			p.sendMessage("You have joined the '" + global.getColoredName() + "' channel.");
			
			Channel local = plugin.getChannel("Local");
			local.join(p);
			p.sendMessage("You have joined the '" + local.getColoredName() + "' channel.");
						
			if (p.isOp()) {
				Channel admin = plugin.getChannel("Admin");
				admin.forceJoin(p);
				p.sendMessage("You have joined the '" + admin.getColoredName() + "' channel.");
			}
		}
		
		if (ChatChannels.ircBot != null && ChatChannels.ircBot.isConnected()) {
			ChatChannels.ircBot.relayAnnouncement(p.getName() + " has joined the game.");
		}
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		if (!event.isCancelled()) {
			Player p = event.getPlayer();
			Channel channel = ChatChannels.activeChannels.get(p.getName());
			if (channel != null) {
				channel.sendMessage(p, event.getMessage());
			} else {
				p.sendMessage("You are not in a channel.");
			}
			event.setCancelled(true);
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player p = event.getPlayer();		

		if (ChatChannels.ircBot != null && ChatChannels.ircBot.isConnected()) {
			ChatChannels.ircBot.relayAnnouncement(p.getName() + " has left the game.");
		}
	}
	
	
	
}
