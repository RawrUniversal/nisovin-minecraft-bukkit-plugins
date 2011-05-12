package com.nisovin.muddersmilk;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MuddersMilk extends JavaPlugin {

	private HashMap<String,Integer> drunks;
	private DrunkEffects effects;
	private int taskId;
	
	@Override
	public void onEnable() {
		drunks = new HashMap<String,Integer>();
		new MilkPlayerListener(this);
	}
	
	public int moreDrunk(Player player) {
		Integer lvl = drunks.get(player.getName());
		if (lvl == null) {
			drunks.put(player.getName(), 1);
			lvl = 1;
		} else {
			drunks.put(player.getName(), lvl+1);
			lvl = lvl+1;
		}
		if (effects == null) {
			startEffects();
		}
		return lvl;
	}
	
	public HashMap<String,Integer> getDrunks() {
		return drunks;
	}
	
	public int getDrunkLevel(Player player) {
		Integer lvl = drunks.get(player.getName());
		if (lvl == null) {
			return 0;
		} else {
			return lvl;
		}
	}
	
	public void startEffects() {
		effects = new DrunkEffects(this);
		taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, effects, 100, 100);
	}
	
	public void stopEffects() {
		getServer().getScheduler().cancelTask(taskId);
		effects = null;
	}

	@Override
	public void onDisable() {
		if (effects != null) {
			stopEffects();
		}
	}

}
