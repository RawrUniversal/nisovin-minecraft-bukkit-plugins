package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

public class HealthBelowCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		return player.getHealth() < Integer.parseInt(var);
	}

}
