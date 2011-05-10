package com.nisovin.MineCal;

import org.bukkit.World;

public class DayWatcher implements Runnable {

	MineCal cal = null;
	World world = null;
	long lastTime = 0;
	int offset = 0;
	boolean ignoreBigChanges;
	int tickInterval;
	
	public DayWatcher(MineCal cal, World world, int offset, boolean ignoreBigChanges, int tickInterval) {
		this.cal = cal;
		this.world = world;
		this.offset = offset;
		this.ignoreBigChanges = ignoreBigChanges;
		this.tickInterval = tickInterval;
		if (world != null) {
			this.lastTime = world.getTime();
		}
	}
	
	@Override
	public void run() {
		if (world != null) {
			boolean newday = false;
			
			long time = world.getTime();
			if (ignoreBigChanges && timeDiff(lastTime, time) > tickInterval*3) {
				newday = false;
			} else if (time - offset < 0) {
				newday = false;
			} else if (lastTime - offset < 0) {
				newday = true;
			} else if (lastTime > time) {
				newday = true;
			}
			/*if (time > lastTime && lastTime % 24000 < offset && time % 24000 > offset && (!ignoreBigChanges || Math.abs(time-lastTime) < tickInterval*3)) {
				newday = true;
			}*/
			lastTime = time;
			
			if (newday) {
				cal.advanceDay();
			}
		} else {
			//System.out.println("MineCal: Error - world does not exist!");
		}
	}
	
	public long timeDiff(long time1, long time2) {
		if (time2 > time1) {
			return time2-time1;
		} else {
			return (time2+24000)-time1;
		}
	}

}
