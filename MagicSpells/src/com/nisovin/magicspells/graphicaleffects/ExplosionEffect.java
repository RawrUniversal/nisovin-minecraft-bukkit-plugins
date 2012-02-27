package com.nisovin.magicspells.graphicaleffects;

import org.bukkit.Location;

public class ExplosionEffect extends GraphicalEffect {

	@Override
	public void showEffect(Location location, String param) {
		location.getWorld().createExplosion(location, 0F);
	}

}
