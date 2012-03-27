package com.nisovin.simplecooldowns;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Warmup extends TimerTask {

	private Timer timer;
	private Player player;
	private Command command;
	private String msg;
	private int warmup;
	private boolean cancelled;
	
	public Warmup(Player player, Command command, String msg) {
		this.player = player;
		this.command = command;
		this.msg = msg;
		this.warmup = command.getWarmup();
		this.cancelled = false;
		start();
	}
	
	public void start() {
		timer = new Timer();
		timer.schedule(this, warmup);
	}
	
	public void stop() {
		cancelled = true;
		timer.cancel();
	}
	
	public boolean interrupt(String source) {
		if (source.equals("damage") && command.cancelWarmupOnDamage()) {
			stop();
			SimpleCooldowns.plugin.sendMessage(player, command.getWarmupInterruptMessage());
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void run() {
		final Warmup thisWarmup = this;
		Bukkit.getScheduler().scheduleSyncDelayedTask(SimpleCooldowns.plugin, new Runnable() {
			@Override
			public void run() {
				if (!cancelled && player.isOnline() && !player.isDead()) {
					player.performCommand(msg.substring(1));
					command.startCooldown(player);
				}
				SimpleCooldowns.plugin.removeWarmup(player, thisWarmup);
			}
		});
	}

}
