package com.nisovin.mobbehaviors.behaviors;

import net.minecraft.server.EntityCreature;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalRandomStroll;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.mobbehaviors.Behavior;

public class BehaviorWander extends Behavior {

	public BehaviorWander(ConfigurationSection config) {
		super(config);
	}

	@Override
	public void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector) {
		selector.a(priority, new PathfinderGoalRandomStroll((EntityCreature) entity));
	}

}
