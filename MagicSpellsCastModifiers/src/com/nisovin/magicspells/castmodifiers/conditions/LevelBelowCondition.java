package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class LevelBelowCondition extends Condition {

	int level = 0;

	@Override
	public boolean setVar(String var) {
		try {
			level = Integer.parseInt(var);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean check(Player player) {
		return player.getLevel() < level;
	}

}
