package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

class LightningEffect extends SpellEffect {

	@Override
	public void loadFromString(String string) {
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
	}

	@Override
	public void playEffect(Location location) {
		location.getWorld().strikeLightningEffect(location);
	}

}
