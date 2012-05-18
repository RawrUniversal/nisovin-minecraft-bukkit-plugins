package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

public class InWorldCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		return player.getWorld().getName().equalsIgnoreCase(var);
	}

}
