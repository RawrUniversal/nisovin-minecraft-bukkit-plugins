package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class LightLevelAboveCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		return (player.getLocation().getBlock().getLightLevel() > Byte.parseByte(var));
	}

}
