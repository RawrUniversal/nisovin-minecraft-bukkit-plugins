package com.nisovin.magicspells.graphicaleffects;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class HeartsEffect extends GraphicalEffect {
	
	@Override
	public void playEffect(Location location, String param) {
		LivingEntity e = location.getWorld().spawnCreature(location, EntityType.OCELOT);
		e.playEffect(EntityEffect.WOLF_HEARTS);
		e.remove();
	}
	
}
