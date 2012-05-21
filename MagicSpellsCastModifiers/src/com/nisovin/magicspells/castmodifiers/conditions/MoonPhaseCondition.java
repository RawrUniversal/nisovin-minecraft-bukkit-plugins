package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class MoonPhaseCondition extends Condition {

	@Override
	public boolean check(Player player, String var) {
		long time = player.getWorld().getFullTime();
		int phase = (int)((time / 24000) % 8);
		if (phase == 0 && var.equalsIgnoreCase("full")) {
			return true;
		} else if ((phase == 1 || phase == 2 || phase == 3) && var.equalsIgnoreCase("waning")) {
			return true;
		} else if (phase == 4 && var.equalsIgnoreCase("new")) {
			return true;
		} else if ((phase == 5 || phase == 6 || phase == 7) && var.equalsIgnoreCase("waxing")) {
			return true;
		} else {
			return false;
		}
	}

}
