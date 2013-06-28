package com.nisovin.magicspells.spells;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface TargetedEntitySpell {
	
	public boolean castAtEntity(Player caster, LivingEntity target, float power);
	
}
