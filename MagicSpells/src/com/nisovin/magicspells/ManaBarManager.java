package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.MagicConfig;

public class ManaBarManager {

	protected static String manaBarPrefix;
	protected static int manaBarSize;
	protected static ChatColor manaBarColorFull;
	protected static ChatColor manaBarColorEmpty;
	protected static int manaBarToolSlot;
	
	private int maxMana;
	private int manaRegenTickRate;
	private int manaRegenAmount;
	private boolean showManaOnUse;
	private boolean showManaOnRegen;
	private boolean showManaOnWoodTool;	
	
	private Map<String,ManaBar> manaBars;
	private int taskId = -1;
	
	public ManaBarManager(MagicConfig config) {
		manaBarPrefix = config.getString("general.mana.mana-bar-prefix", "Mana:");
		manaBarSize = config.getInt("general.mana.mana-bar-size", 35);
		manaBarColorFull = ChatColor.getByChar(config.getString("general.mana.color-full", ChatColor.GREEN.getChar() + ""));
		manaBarColorEmpty = ChatColor.getByChar(config.getString("general.mana.color-empty", ChatColor.BLACK.getChar() + ""));
		manaBarToolSlot = config.getInt("general.mana.tool-slot", 8);
		
		maxMana = config.getInt("general.mana.max-mana", 100);
		manaRegenTickRate = config.getInt("general.mana.regen-tick-rate", 100);
		manaRegenAmount = config.getInt("general.mana.regen-amount", 5);
		showManaOnUse = config.getBoolean("general.mana.show-mana-on-use", false);
		showManaOnRegen = config.getBoolean("general.mana.show-mana-on-regen", false);
		showManaOnWoodTool = config.getBoolean("general.mana.show-mana-on-wood-tool", true);
		
		manaBars = new HashMap<String,ManaBar>();
		startRegenerator();
	}
	
	public void createManaBar(Player player) {
		if (!manaBars.containsKey(player.getName())) {
			manaBars.put(player.getName(), new ManaBar(maxMana));
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
		showMana(player, false);
	}
	
	public void showMana(Player player, boolean forceShowInChat) {
		ManaBar bar = manaBars.get(player.getName());
		if (bar != null) {
			if (forceShowInChat || showManaOnUse) {
				bar.showInChat(player);
			}
			if (showManaOnWoodTool) {
				bar.showOnTool(player);
			}
			// send event
			bar.callManaChangeEvent(player);
		}
	}
	
	public void startRegenerator() {
		stopRegenerator();
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, new ManaBarRegenerator(), manaRegenTickRate, manaRegenTickRate);
	}
	
	public void stopRegenerator() {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}
	
	public void turnOff() {
		stopRegenerator();
		manaBars.clear();
		manaBars = null;
	}
	
	private class ManaBarRegenerator implements Runnable {
		public void run() {
			for (String p: manaBars.keySet()) {
				ManaBar bar = manaBars.get(p);
				boolean regenerated = bar.add(manaRegenAmount);
				if (regenerated) {
					Player player = Bukkit.getServer().getPlayer(p);
					if (player != null && player.isOnline()) {
						if (showManaOnRegen) {
							bar.showInChat(player);
						}
						if (showManaOnWoodTool) {
							bar.showOnTool(player);
						}
					}
					bar.callManaChangeEvent(player);
				}
			}
		}
	}

}