package com.nisovin.MagicSpells;

public class ManaBarManager {

	private HashMap<String,ManaBar> manaBars;
	private int taskId;
	
	public ManaBarManager() {
		manaBars = HashMap<String,ManaBar>();
		startRegenerator();
	}
	
	public void createManaBar(Player player) {
		if (!manaBars.containsKey(player.getName())) {
			manaBars.put(player.getName(), new ManaBar(player));
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
			return manaBars.get(player.getName()).remove(amount);
		}
	}
	
	public void showMana(Player player) {
		ManaBar bar = manaBars.get(player.getName());
		if (bar != null) {
			bar.show(player);
		}
	}
	
	public void startRegenerator() {
		taskId = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(MagicSpells.plugin, new ManaBarRegenerator(), MagicSpells.manaRegenTickRate, MagicSpells.manaRegenTickRate);
	}
	
	public void stopRegenerator() {
		Bukkit.getServer().getScheduler().stopTask(taskId);
	}
	
	private class ManaBarRegenerator implements Runnable {
		private void run() {
			for (ManaBar bar : manaBars.values()) {
				bar.regenerate(MagicSpells.manaRegenPercent);
			}
		}
	}

}