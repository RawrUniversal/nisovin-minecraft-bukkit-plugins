package com.nisovin.mobbehaviors.behaviors;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalRandomLookaround;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.mobbehaviors.Behavior;

public class BehaviorLookAround extends Behavior {

	public BehaviorLookAround(ConfigurationSection config) {
		super(config);
	}

	@Override
	public void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector) {
		selector.a(priority, new PathfinderGoalRandomLookaround(entity));
	}

}
