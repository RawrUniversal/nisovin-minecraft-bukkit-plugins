package com.nisovin.mobbehaviors.behaviors;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalLookAtPlayer;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.mobbehaviors.Behavior;

public class BehaviorLookAt extends Behavior {

	float range;
	
	public BehaviorLookAt(ConfigurationSection config) {
		super(config);		
		range = (float)config.getDouble("Range", 8.0);
	}

	@Override
	public void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector) {
		selector.a(priority, new PathfinderGoalLookAtPlayer(entity, entity.world, range));
	}

}
