package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.MagicXpHandler;
import com.nisovin.magicspells.castmodifiers.Condition;

public class MagicXpAboveCondition extends Condition {

	static MagicXpHandler handler;

	String school;
	int amount;
	
	@Override
	public boolean setVar(String var) {
		try {
			handler = MagicSpells.getMagicXpHandler();
			if (handler == null) return false;
			
			String[] split = var.split(":");
			school = split[0];
			amount = Integer.parseInt(split[1]);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean check(Player player) {
		return handler.getXp(player, school) >= amount;
	}

	@Override
	public boolean check(Player player, LivingEntity target) {
		if (target instanceof Player) {
			return check((Player)target);
		}
		return false;
	}

	@Override
	public boolean check(Player player, Location location) {
		return false;
	}

}
