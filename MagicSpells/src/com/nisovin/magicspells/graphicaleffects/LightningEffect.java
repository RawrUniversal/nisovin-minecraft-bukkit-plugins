package com.nisovin.magicspells.graphicaleffects;

import org.bukkit.Location;

public class LightningEffect extends GraphicalEffect {

	@Override
	public void playEffect(Location location, String param) {
		location.getWorld().strikeLightningEffect(location);
	}

}
