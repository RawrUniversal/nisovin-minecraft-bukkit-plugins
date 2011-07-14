package com.nisovin.MagicSpells.Util;

import org.bukkit.Bukkit;

import com.nisovin.MagicSpells.MagicSpells;

public abstract class SpellAnimation implements Runnable {

	private int taskId;
	private long delay;
	private long interval;
	private int tick;
	
	public SpellAnimation(long delay, long interval) {
		this(delay, interval, false);
	}
	
	public SpellAnimation(long delay, long interval, boolean autoStart) {
		this.delay = delay;
		this.interval = interval;
		this.tick = -1;
		if (autoStart) {
			play();
		}
	}
	
	public void play() {
		taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, delay, interval);
	}
	
	protected void stop() {
		Bukkit.getServer().getScheduler().cancelTask(taskId);
	}
	
	protected abstract void onTick(int tick);
	
	@Override
	public void run() {
		onTick(++tick);
	}
	
}
