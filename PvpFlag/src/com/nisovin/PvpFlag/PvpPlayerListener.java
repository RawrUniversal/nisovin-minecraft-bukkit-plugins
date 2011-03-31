package com.nisovin.PvpFlag;

import org.bukkit.ChatColor;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;

public class PvpPlayerListener extends PlayerListener {

	private PvpFlag plugin;

	public PvpPlayerListener(PvpFlag plugin) {		
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		if (plugin.COLOR_NAMEPLATES) {
			plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Normal, plugin);
		}
	
	}
	
	public void onPlayerJoin(PlayerEvent event) {
		String s = plugin.loginMessage.get(event.getPlayer().getName());
		if (s != null && !s.equals("")) {
			event.getPlayer().sendMessage(s);
		}
		plugin.loginMessage.remove(event.getPlayer().getName());
	}
	
	public void onPlayerCommandPreprocess(PlayerChatEvent event) {
		if (event.getMessage().equals("_nameplate_color_check")) {
			if (plugin.isFlagged(event.getPlayer())) {
				event.setMessage("_nameplate_color_set_" + ChatColor.RED.getCode());
			}
			event.setCancelled(true);
		}
	}

}