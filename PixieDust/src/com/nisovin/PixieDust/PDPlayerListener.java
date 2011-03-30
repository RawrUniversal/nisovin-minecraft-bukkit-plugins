package com.nisovin.PixieDust;


import java.util.HashMap;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.util.Vector;


public class PDPlayerListener extends PlayerListener {

	private PixieDust plugin;
	public static HashMap<String, Integer> flyers = new HashMap<String, Integer>();
	
	public PDPlayerListener(PixieDust plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		Player p = event.getPlayer();
		if (p.isOp() && p.getLocation().getPitch() < PixieDust.ACTIVATION_PITCH*-1 && p.getItemInHand().getTypeId() == PixieDust.FLY_ITEM) {
			if (flyers.containsKey(p.getName())) {
				plugin.getServer().getScheduler().cancelTask(flyers.get(p.getName()));
				flyers.remove(p.getName());
				p.sendMessage(PixieDust.FLY_STOP);
			} else {
				int task = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new FlightTicker(p, plugin.getServer()), PixieDust.TICK_DELAY, PixieDust.TICK_INTERVAL);
				if (task != -1) {
					flyers.put(p.getName(), task);
					p.sendMessage(PixieDust.FLY_START);
				}
			}
		}
	}
		
	public class FlightTicker implements Runnable {

		String playerName;
		Server server;
		
		public FlightTicker(Player p, Server s) {
			playerName = p.getName();
			server = s;
		}
		
		@Override
		public void run() {
			Player p = server.getPlayer(playerName);
			
			float speed = PixieDust.FLY_SPEED / 10F;
			
			if (!PixieDust.REQUIRE_ITEM_WHILE_FLYING || p.getItemInHand().getTypeId() == PixieDust.FLY_ITEM) {
				Vector v = p.getLocation().getDirection();
				
				v.setX(v.getX()*speed);
				v.setY((v.getY()+(PixieDust.Y_OFFSET / 10F))*speed);
				v.setZ(v.getZ()*speed);
				
				p.setVelocity(v);
			}			
			
		}
		
	}
}
