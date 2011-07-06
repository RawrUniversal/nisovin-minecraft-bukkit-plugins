package com.nisovin.MagicSpells;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ManaBarManager {

	private HashMap<String,ManaBar> manaBars;
	private int taskId;
	
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
				if (MagicSpells.showManaOnUse) {
					showMana(player);
				}
				if (MagicSpells.showManaOnWoodTool) {
					showManaOnTool(player);
				}
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
				if (MagicSpells.showManaOnUse) {
					showMana(player);
				}
				if (MagicSpells.showManaOnWoodTool) {
					showManaOnTool(player);
				}
			}
			return r;
		}		
	}
	
	public void showMana(Player player) {
		ManaBar bar = manaBars.get(player.getName());
		if (bar != null) {
			bar.show(player);
		}
	}
	
	public void showManaOnTool(Player player) {
		ManaBar bar = manaBars.get(player.getName());
		if (bar != null) {
			bar.showOnTool(player);
		}		
	}
	
	public void startRegenerator() {
		taskId = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(MagicSpells.plugin, new ManaBarRegenerator(), MagicSpells.manaRegenTickRate, MagicSpells.manaRegenTickRate);
	}
	
	public void stopRegenerator() {
		Bukkit.getServer().getScheduler().cancelTask(taskId);
	}
	
	private class ManaBarRegenerator implements Runnable {
		public void run() {
			for (String p: manaBars.keySet()) {
				ManaBar bar = manaBars.get(p);
				boolean regenerated = bar.regenerate(MagicSpells.manaRegenPercent);
				if (regenerated && (MagicSpells.showManaOnRegen || MagicSpells.showManaOnWoodTool)) {
					Player player = Bukkit.getServer().getPlayer(p);
					if (player != null) {
						if (MagicSpells.showManaOnRegen) {
							showMana(player);
						}
						if (MagicSpells.showManaOnWoodTool) {
							showManaOnTool(player);
						}						
					}
				}
			}
		}
	}

}