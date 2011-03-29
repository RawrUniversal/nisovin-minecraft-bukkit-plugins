package com.nisovin.PixieDust;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PDEntityListener extends EntityListener {

	private PixieDust plugin;
	
	public PDEntityListener(PixieDust plugin) {
		this.plugin = plugin;
		plugin.getServer().getLogger().info("test");
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && event.getCause() == DamageCause.FALL) {
			Player p = (Player)event.getEntity();
			
			plugin.getServer().getScheduler().cancelTask(PDPlayerListener.flyers.get(p.getName()));
			PDPlayerListener.flyers.remove(p.getName());
			p.sendMessage(PixieDust.FLY_STOP);
			
			event.setCancelled(true);
		}
	}
	
}
