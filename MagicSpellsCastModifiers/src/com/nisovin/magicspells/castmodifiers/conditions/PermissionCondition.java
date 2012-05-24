package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class PermissionCondition extends Condition {

	String perm;

	@Override
	public boolean setVar(String var) {
		perm = var;
		return true;
	}
	
	@Override
	public boolean check(Player player) {
		return player.hasPermission(perm);
	}

}
