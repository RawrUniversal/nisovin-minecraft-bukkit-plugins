package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

public class LightLevelBelowCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		return (player.getLocation().getBlock().getLightLevel() < Byte.parseByte(var));
	}

}
