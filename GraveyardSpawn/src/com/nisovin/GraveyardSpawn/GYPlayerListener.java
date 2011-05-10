package com.nisovin.GraveyardSpawn;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class GYPlayerListener extends PlayerListener {
	
	private GraveyardSpawn plugin;
	
	public GYPlayerListener(GraveyardSpawn plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		
		if (plugin.graveyards.size() > 0) {
			Graveyard closest = new Graveyard("temphome", event.getRespawnLocation());
			double dist = closest.calculateDistanceFrom(p);
			
			for (Graveyard gy : plugin.graveyards) {
				double thisDist = gy.calculateDistanceFrom(p);
				if (thisDist != -1 && (closest == null || thisDist < dist)) {
					closest = gy;
					dist = thisDist;
				}
			}
			
			if (closest != null) {
				//plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeathRespawn(p, p.getLocation()), 10);
				event.setRespawnLocation(closest.getLocation());
			}
		}
	}
	
	public class DeathRespawn implements Runnable {
		String playerName;
		Location location;
		public DeathRespawn(Player p, Location l) {
			playerName = p.getName();
			location = l;
		}
		
		public void run() {
			Player player = plugin.getServer().getPlayer(playerName);
			player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			player.setCompassTarget(location);
			player.sendMessage("You have have respawned at a graveyard!");
			player.sendMessage("Use the compass to find your way back to where you died.");
		}
	}
	
}
