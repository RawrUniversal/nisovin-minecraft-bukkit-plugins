package com.nisovin.magicspells.spelleffects;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import com.nisovin.magicspells.MagicSpells;

class BlueSparkleEffect extends SpellEffect {

	@Override
	public void playEffect(Location location, String param) {
		MagicSpells.getVolatileCodeHandler().playEntityAnimation(location, EntityType.WITCH, 15, param != null && param.equals("instant"));
	}
	
}
