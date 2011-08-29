package com.nisovin.IronGates;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class IGEntityListener extends EntityListener {

	IronGates plugin;
	
	public IGEntityListener(IronGates plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player p = (Player)event.getEntity();
			if (plugin.immunity.containsKey(p.getName()) && plugin.immunity.get(p.getName()) > System.currentTimeMillis()) {
				event.setCancelled(true);
			}
		}
	}
	
}
