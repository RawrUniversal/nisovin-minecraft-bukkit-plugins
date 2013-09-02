package com.nisovin.magicspells.spelleffects;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Tameable;

import com.nisovin.magicspells.MagicSpells;

class WolfSmokeEffect extends SpellEffect {

	@Override
	public void loadFromString(String string) {
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {
	}

	@Override
	public void playEffect(Entity entity) {
		if (entity instanceof Tameable) {
			entity.playEffect(EntityEffect.WOLF_SMOKE);
		} else {
			playEffect(entity.getLocation());
		}
	}
	
	@Override
	public void playEffect(Location location) {
		MagicSpells.getVolatileCodeHandler().playEntityAnimation(location, EntityType.OCELOT, EntityEffect.WOLF_SMOKE.getData(), false);
	}
	
}
