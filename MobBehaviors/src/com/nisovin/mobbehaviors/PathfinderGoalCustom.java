package com.nisovin.mobbehaviors;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalFloat;

public class PathfinderGoalCustom extends PathfinderGoalFloat {

	private CustomBehavior goal;
	
	public PathfinderGoalCustom(EntityLiving entity, CustomBehavior goal) {
		super(entity);
		this.goal = goal;
	}
	
	@Override
	public boolean a() {
		return goal.doesBehaviorApply();
	}
	
	/*@Override
	public boolean b() {
		return goal.doesGoalStillApply();
	}*/
	
	@Override
	public void d() { // disable
		goal.executeGoalLastTime();
	}
	
	@Override
	public void e() { // first execute
		goal.executeGoalFirstTime();
	}
	
	@Override
	public void b() { // continuing execute
		goal.executeGoalOngoing();
	}

}
