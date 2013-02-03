package com.nisovin.yapp;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

class PermListener implements Listener {

	private YAPP plugin;
	
	public PermListener(YAPP plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if (!plugin.hasLoadedPermissions(player)) {
			plugin.loadPlayerPermissions(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!plugin.hasLoadedPermissions(player)) {
			plugin.loadPlayerPermissions(player);
		}
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
