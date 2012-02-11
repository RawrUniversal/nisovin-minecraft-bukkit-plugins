package com.nisovin.mobbehaviors;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;

public abstract class Behavior {
	
	protected int priority;
	
	public Behavior(ConfigurationSection config) {
		priority = config.getInt("Priority");
	}
	
	public int getPriority() {
		return priority;
	}
	
	public abstract void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector);
	
}
