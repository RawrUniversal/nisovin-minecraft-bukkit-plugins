package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

public class NightCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		long time = player.getWorld().getTime();
		return (time > 13000 && time < 23000);
	}
	
}
