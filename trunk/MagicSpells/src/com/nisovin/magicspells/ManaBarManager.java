package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ManaBarManager {

	private HashMap<String,ManaBar> manaBars;
	//private int taskId;
	private Timer timer;
	
	public ManaBarManager() {
		manaBars = new HashMap<String,ManaBar>();
		startRegenerator();
	}
	
	public void createManaBar(Player player) {
		if (!manaBars.containsKey(player.getName())) {
			manaBars.put(player.getName(), new ManaBar(MagicSpells.maxMana));
		}
	}
	
	public boolean hasMana(Player player, int amount) {
		if (!manaBars.containsKey(player.getName())) {
			return false;
		} else {
			return manaBars.get(player.getName()).has(amount);
		}
	}
	
	public boolean removeMana(Player player, int amount) {
		if (!manaBars.containsKey(player.getName())) {
			return false;
		} else {
			boolean r = manaBars.get(player.getName()).remove(amount);
			if (r) {
				showMana(player);
			}
			return r;
		}
	}
	
	public boolean addMana(Player player, int amount) {
		if (!manaBars.containsKey(player.getName())) {
			return false;
		} else {
			boolean r = manaBars.get(player.getName()).add(amount);
			if (r) {
				showMana(player);
			}
			return r;
		}		
	}
	
	public void showMana(Player player) {
		ManaBar bar = manaBars.get(player.getName());
		if (bar != null) {
			if (MagicSpells.showManaOnUse) {
				bar.showInChat(player);
			}
			if (MagicSpells.showManaOnWoodTool) {
				bar.showOnTool(player);
			}
			// send event
			bar.callManaChangeEvent(player);
		}
	}
	
	public void startRegenerator() {
		if (timer != null) {
			stopRegenerator();
		}
		timer = new Timer();
		TimerTask task = new ManaBarRegenerator();
		timer.schedule(task, 0, MagicSpells.manaRegenTickRate);
	}
	
	public void stopRegenerator() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}
	
	private class ManaBarRegenerator extends TimerTask {
		public void run() {
			for (String p: manaBars.keySet()) {
				ManaBar bar = manaBars.get(p);
				boolean regenerated = bar.regenerate(MagicSpells.manaRegenPercent);
				if (regenerated && (MagicSpells.showManaOnRegen || MagicSpells.showManaOnWoodTool)) {
					Player player = Bukkit.getServer().getPlayer(p);
					if (player != null && player.isOnline()) {
						showMana(player);
					}
				}
			}
		}
	}

}