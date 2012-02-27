package com.nisovin.magicspells.graphicaleffects;

import org.bukkit.Effect;
import org.bukkit.Location;

public class MobSpawnerEffect extends GraphicalEffect {

	@Override
	public void showEffect(Location location, String param) {
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
	}
	
}
