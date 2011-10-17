package com.nisovin.IronGates;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class IGPlayerListener extends PlayerListener {

	private IronGates plugin;
	
	public IGPlayerListener(IronGates plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Player p = event.getPlayer();
		
		Location l = p.getLocation();
		String s = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		
		Gate gate = plugin.gates.get(s);
		if (gate != null) {
			gate.teleportPlayerToExit(p);
		}
	}
	
	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		Location l = p.getLocation();
		String s = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		
		Gate gate = plugin.gates.get(s);
		if (gate != null && !p.hasPermission("irongates.deny." + gate.getName())) {
			ItemStack inHand = p.getItemInHand();
			if (gate.key == -1 || inHand.getTypeId() == gate.key) {
				if (gate.consumeKey() && gate.key > 0) {
					if (inHand.getAmount() == 1) {
						p.setItemInHand(null);
					} else {
						inHand.setAmount(inHand.getAmount()-1);
						p.setItemInHand(inHand);
					}
				}
				final Location loc = gate.getExit();
				event.setTo(loc);
				plugin.immunity.put(p.getName(), System.currentTimeMillis() + 5000);
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						Chunk chunk = loc.getWorld().getChunkAt(loc);
						chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
					}
				}, 5);
			}
		}
	}
	
}
