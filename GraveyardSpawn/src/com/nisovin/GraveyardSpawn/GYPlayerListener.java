package com.nisovin.GraveyardSpawn;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GYPlayerListener extends PlayerListener {
	
	private GraveyardSpawn plugin;
		
	public GYPlayerListener(GraveyardSpawn plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player p = event.getPlayer();
		
		Location location = p.getLocation();
		if (plugin.deathLocation.containsKey(p.getName())) {
			location = plugin.deathLocation.get(p.getName());
			plugin.deathLocation.remove(p.getName());
		}
		if (plugin.graveyards.size() > 0) {
			Graveyard closest = new Graveyard("temphome", event.getRespawnLocation());
			double dist = closest.calculateDistanceFrom(location);
			for (Graveyard gy : plugin.graveyards) {
				if (p.hasPermission("gy.spawn." + gy.getName())) {
					double thisDist = gy.calculateDistanceFrom(location);
					if (thisDist != -1 && (closest == null || dist == -1 || thisDist < dist)) {
						closest = gy;
						dist = thisDist;
					}
				}
			}
			
			if (closest != null) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new DeathRespawn(p, p.getLocation()), 5);
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
			/*Player player = plugin.getServer().getPlayer(playerName);
			player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
			player.setCompassTarget(location);
			player.sendMessage("You have have respawned at a graveyard!");
			player.sendMessage("Use the compass to find your way back to where you died.");*/
			Chunk chunk = location.getWorld().getChunkAt(location);
			chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
		}
	}
	
}
