package com.nisovin.PvpFlag;

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
			Player attacker = event.getDamager();
			Player defender = event.getEntity();
			
			if (!plugin.isFlagged(defender) || !plugin.isFlagged(attacker)) {
				event.setCancelled(true);
			} else {
				plugin.setLastActivity(attacker);
				plugin.setLastActivity(defender);
			}
		}
	}

}