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
			Graveyard closest = null;
			double dist = 0;
			
			for (Graveyard gy : plugin.graveyards) {
				double thisDist = gy.calculateDistanceFrom(p);
				if (thisDist != -1 && (closest == null || thisDist < dist)) {
					closest = gy;
					dist = thisDist;
				}
			}
			
			if (closest != null) {
				//plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new DeathRespawn(p, p.getLocation()), 50);
				event.setRespawnLocation(closest.getLocation());
			}
		}
	}
	
	public class DeathRespawn implements Runnable {
		Player player;
		Location location;
		public DeathRespawn(Player p, Location l) {
			player = p;
			location = l;
		}
		
		public void run() {
			//player.sendMessage("You're alive!");
			player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			//player.updateInventory();
			player.setCompassTarget(location);
		}
	}
	
}
