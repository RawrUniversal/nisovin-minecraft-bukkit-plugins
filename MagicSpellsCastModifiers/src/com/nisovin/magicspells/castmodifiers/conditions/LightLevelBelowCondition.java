package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class LightLevelBelowCondition extends Condition {

	byte level = 0;

	@Override
	public boolean setVar(String var) {
		try {
			level = Byte.parseByte(var);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return (player.getLocation().getBlock().getLightLevel() < level);
	}

}
