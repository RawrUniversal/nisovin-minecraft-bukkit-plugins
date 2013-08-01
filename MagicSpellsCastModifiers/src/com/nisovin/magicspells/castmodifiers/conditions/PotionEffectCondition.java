package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.nisovin.magicspells.castmodifiers.Condition;

public class PotionEffectCondition extends Condition {

	PotionEffectType effectType;
	
	@Override
	public boolean setVar(String var) {
		if (var.matches("^[0-9]+$")) {
			int type = Integer.parseInt(var);
			effectType = PotionEffectType.getById(type);
		}
		return effectType != null;
	}

	@Override
	public boolean check(Player player) {
		return player.hasPotionEffect(effectType);
	}

	
	
}
