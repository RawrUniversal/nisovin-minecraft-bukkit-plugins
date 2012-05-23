package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class HealthBelowCondition extends Condition {
	
	int health = 0;

	@Override
	public void setVar(String var) {
		health = Integer.parseInt(var);
	}

	@Override
	public boolean check(Player player) {
		return player.getHealth() < health;
	}

}
