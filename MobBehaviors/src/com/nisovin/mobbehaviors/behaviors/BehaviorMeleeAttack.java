package com.nisovin.mobbehaviors.behaviors;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalMeleeAttack;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.mobbehaviors.Behavior;

public class BehaviorMeleeAttack extends Behavior {

	private float speed;
	private boolean flag;
	
	public BehaviorMeleeAttack(ConfigurationSection config) {
		super(config);
		speed = (float)config.getDouble("Speed", 16.0);
		flag = config.getBoolean("Flag", true);
	}

	@Override
	public void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector) {
		selector.a(priority, new PathfinderGoalMeleeAttack(entity, EntityHuman.class, speed, flag));
	}

}
