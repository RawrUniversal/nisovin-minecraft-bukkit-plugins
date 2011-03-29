package com.nisovin.IronGates;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerListener;

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
	
}
