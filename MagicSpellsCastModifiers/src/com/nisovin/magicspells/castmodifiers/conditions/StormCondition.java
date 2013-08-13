package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class StormCondition extends Condition {

	@Override
	public boolean setVar(String var) {
		return true;
	}

	@Override
	public boolean check(Player player) {
		return player.getWorld().hasStorm() || player.getWorld().isThundering();
	}
	
	@Override
	public boolean check(Player player, LivingEntity target) {
		return check(player);
	}

}
