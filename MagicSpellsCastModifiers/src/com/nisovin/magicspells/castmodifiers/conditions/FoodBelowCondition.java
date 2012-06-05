package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class FoodBelowCondition extends Condition {

	int food = 0;

	@Override
	public boolean setVar(String var) {
		try {
			food = Integer.parseInt(var);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean check(Player player) {
		return player.getFoodLevel() < food;
	}

}
