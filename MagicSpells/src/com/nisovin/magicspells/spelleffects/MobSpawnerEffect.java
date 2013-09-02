package com.nisovin.magicspells.spelleffects;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

class MobSpawnerEffect extends SpellEffect {

	@Override
	public void loadFromString(String string) {
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
	}

	@Override
	public void playEffect(Location location) {
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
	}
	
}
