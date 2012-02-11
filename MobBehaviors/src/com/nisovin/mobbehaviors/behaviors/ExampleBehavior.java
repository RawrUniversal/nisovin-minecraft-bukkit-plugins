package com.nisovin.mobbehaviors.behaviors;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.mobbehaviors.CustomBehavior;

public class ExampleBehavior extends CustomBehavior {

	public ExampleBehavior(ConfigurationSection config) {
		super(config);
	}

	@Override
	public boolean doesBehaviorApply() {
		return (!getEntity().isDead());
	}

	@Override
	public void executeGoalFirstTime() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeGoalOngoing() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeGoalLastTime() {
		// TODO Auto-generated method stub
		
	}

}
