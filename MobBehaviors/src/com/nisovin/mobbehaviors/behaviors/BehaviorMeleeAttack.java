package com.nisovin.mobbehaviors.behaviors;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;
import net.minecraft.server.PathfinderGoalMeleeAttack;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.mobbehaviors.Behavior;

public class BehaviorMeleeAttack extends Behavior {

	private float range;
	
	public BehaviorMeleeAttack(ConfigurationSection config) {
		super(config);
		range = (float)config.getDouble("Range", 16.0);
	}

	@Override
	public void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector) {
		selector.a(priority, new PathfinderGoalMeleeAttack((EntityMonster)entity, entity.world, range));
	}

}
