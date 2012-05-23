package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class StormCondition extends Condition {

	@Override
	public void setVar(String var) {
	}

	@Override
	public boolean check(Player player) {
		return player.getWorld().hasStorm() || player.getWorld().isThundering();
	}

}
