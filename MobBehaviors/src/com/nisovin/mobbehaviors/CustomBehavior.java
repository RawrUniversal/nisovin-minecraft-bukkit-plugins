package com.nisovin.mobbehaviors;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public abstract class CustomBehavior extends Behavior {

	private LivingEntity entity;
	
	public CustomBehavior(ConfigurationSection config) {
		super(config);
	}

	public LivingEntity getEntity() {
		return entity;
	}
	
	public abstract boolean doesBehaviorApply();
	
	public boolean doesBehaviorStillApply() {
		return doesBehaviorApply();
	}
	
	public abstract void executeGoalFirstTime();
	
	public abstract void executeGoalOngoing();
	
	public abstract void executeGoalLastTime();
	
	@Override
	public void addGoalToEntity(EntityLiving entity, PathfinderGoalSelector selector) {
		selector.a(priority, new PathfinderGoalCustom(this));
		this.entity = (LivingEntity)entity.getBukkitEntity();
	}

}
