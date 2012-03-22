package com.nisovin.yapp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PermListener implements Listener {

	private MainPlugin plugin;
	
	public PermListener(MainPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.loadPlayerPermissions(event.getPlayer());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		plugin.loadPlayerPermissions(event.getPlayer());
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.unloadPlayer(event.getPlayer());
	}
	
}
