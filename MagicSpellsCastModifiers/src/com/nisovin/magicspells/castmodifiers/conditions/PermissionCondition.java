package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class PermissionCondition extends Condition {

	String perm;

	@Override
	public void setVar(String var) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean check(Player player) {
		return player.hasPermission(perm);
	}

}
