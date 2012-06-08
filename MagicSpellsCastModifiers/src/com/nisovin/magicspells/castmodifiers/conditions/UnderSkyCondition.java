package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class UnderSkyCondition extends Condition {

	@Override
	public boolean setVar(String var) {
		return true;
	}

	@Override
	public boolean check(Player player) {
		return player.getWorld().getHighestBlockYAt(player.getLocation()) <= player.getEyeLocation().getY();
	}

}
