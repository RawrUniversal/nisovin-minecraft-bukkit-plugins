package com.nisovin.PvpFlag;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class PvpEntityListener extends EntityListener {

	private PvpFlag plugin;
	
	public PvpEntityListener(PvpFlag plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Normal, plugin);
	}
	
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK && event instanceof EntityDamageByEntityEvent) {
			onEntityDamageByEntity((EntityDamageByEntityEvent)event);
		}
	}
	
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!event.isCancelled() && event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
			Player attacker = (Player)event.getDamager();
			Player defender = (Player)event.getEntity();
			
			if ( (!plugin.isFlagged(defender) || !plugin.isFlagged(attacker)) && (!attacker.isOp() || !plugin.OP_OVERRIDE) ) {
				event.setCancelled(true);
			} else {
				plugin.setLastActivity(attacker);
				plugin.setLastActivity(defender);
			}
		}
	}

}