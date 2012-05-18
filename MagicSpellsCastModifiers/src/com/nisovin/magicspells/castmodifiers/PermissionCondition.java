package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

public class PermissionCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		return player.hasPermission(var);
	}

}
