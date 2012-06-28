package com.nisovin.mobbehaviors;

import net.minecraft.server.PathfinderGoal;

public class PathfinderGoalCustom extends PathfinderGoal {

	private CustomBehavior goal;
	
	public PathfinderGoalCustom(CustomBehavior goal) {
		this.goal = goal;
	}
	
	@Override
	public boolean a() {
		return goal.doesBehaviorApply();
	}
	
	@Override
	public boolean b() {
		return goal.doesBehaviorStillApply();
	}
	
	@Override
	public void e() { // first execute
		goal.executeGoalFirstTime();
	}
	
	@Override
	public void c() { // continuing execute
		goal.executeGoalOngoing();
	}
	
	@Override
	public void d() { // disable
		goal.executeGoalLastTime();
	}

}
