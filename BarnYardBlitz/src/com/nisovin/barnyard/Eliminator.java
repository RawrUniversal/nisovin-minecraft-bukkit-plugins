package com.nisovin.barnyard;

import org.bukkit.Bukkit;

public class Eliminator implements Runnable {

	boolean stopped = false;
	BarnYardBlitz plugin;
	
	public Eliminator(BarnYardBlitz plugin) {
		this.plugin = plugin;
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < plugin.eliminationIntervals.length; i++) {
				Thread.sleep(plugin.eliminationIntervals[i] * 1000);
				if (stopped) return;
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						plugin.calculateScoresAndDoElimination();
					}
				}, 1);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		stopped = true;
	}
	
}
