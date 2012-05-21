package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class DayCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		long time = player.getWorld().getTime();
		return !(time > 13000 && time < 23000);
	}

}
