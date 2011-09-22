package com.nisovin.oldgods;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.PluginManager;

import com.nisovin.oldgods.godhandlers.*;

public class PListener extends PlayerListener {

	OldGods plugin;
	
	public PListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.PLAYER_TOGGLE_SPRINT, this, Event.Priority.High, plugin);
		//pm.registerEvent(Event.Type.PLAYER_MOVE, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.showCurrentGod(event.getPlayer());
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().isSneaking() && event.getPlayer().getItemInHand().getType() == Material.AIR) {
			plugin.altars().pray(event.getPlayer(), event.getClickedBlock());
		}
	}
	
	@Override
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.EXPLORATION) {
			ExplorationHandler.onPlayerToggleSprint(event);
		}		
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.EXPLORATION) {
			ExplorationHandler.onPlayerMove(event);
		}
	}
	
	
	
}
