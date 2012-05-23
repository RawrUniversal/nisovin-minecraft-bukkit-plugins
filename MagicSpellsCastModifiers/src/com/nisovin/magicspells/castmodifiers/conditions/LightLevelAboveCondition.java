package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.Condition;

public class LightLevelAboveCondition extends Condition {

	byte level = 0;

	@Override
	public void setVar(String var) {
		level = Byte.parseByte(var);
	}
	
	@Override
	public boolean check(Player player) {
		return (player.getLocation().getBlock().getLightLevel() > level);
	}

}
