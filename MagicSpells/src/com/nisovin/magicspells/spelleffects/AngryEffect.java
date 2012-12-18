package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.nisovin.magicspells.MagicSpells;

class AngryEffect extends SpellEffect {

	@Override
	public void playEffect(Location location, String param) {
		MagicSpells.getVolatileCodeHandler().playEntityAnimation(location, EntityType.VILLAGER, 13, param != null && param.equals("instant"));
	}
	
}
