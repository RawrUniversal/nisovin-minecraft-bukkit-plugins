package com.nisovin.barnyard;

import org.bukkit.Bukkit;

public class Eliminator implements Runnable {

	boolean stopped = false;
	BarnYardBlitz plugin;
	long roundStart = 0;
	long nextElimination = 0;
	
	public Eliminator(BarnYardBlitz plugin) {
		this.plugin = plugin;
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < plugin.eliminationIntervals.length; i++) {
				roundStart = System.currentTimeMillis();
				nextElimination = System.currentTimeMillis() + plugin.eliminationIntervals[i] * 1000;
				Thread.sleep(plugin.eliminationIntervals[i] * 1000);
				if (stopped) return;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						plugin.calculateScoresAndDoElimination();
					}
				}, 1);
			}
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					plugin.stopGame();
				}
			}, 5);
			Thread.sleep(10000);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					plugin.endGame();
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public long timeToNextElimination() {
		if (nextElimination == 0) {
			return 0;
		} else {
			return (nextElimination - System.currentTimeMillis()) / 1000;
		}
	}
	
	public double getPercentElapsedTime() {
		return ((double)System.currentTimeMillis() - roundStart) / (double)(nextElimination - roundStart);
	}
	
	public void stop() {
		stopped = true;
		nextElimination = 0;
	}
	
}
