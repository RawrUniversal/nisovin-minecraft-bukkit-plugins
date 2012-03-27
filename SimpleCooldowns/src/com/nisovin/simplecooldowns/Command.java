package com.nisovin.simplecooldowns;

import java.util.HashMap;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class Command {

	private String name;
	private boolean caseSensitive;
	private boolean usePermission;
	private String pattern;
	
	private int warmup;
	private String warmupMessage;
	private boolean interruptWarmupOnDamage;
	private String warmupInterruptMessage;
	//private boolean cancelWarmupOnMove;
	
	private int cooldown;
	private String cooldownMessage;
	private boolean cooldownMessageMinutes;
	private boolean cooldownMessageHours;
	
	private HashMap<String, Long> cooldowns;
	
	public Command(String name, ConfigurationSection config) {
		this.name = name;
		caseSensitive = config.getBoolean("case-sensitive", false);
		usePermission = config.getBoolean("use-permission", false);
		
		pattern = config.getString("command");
		if (!config.getBoolean("regex", false)) {
			if (!caseSensitive) {
				pattern = pattern.toLowerCase();
			}
			pattern = "^\\Q" + pattern + "\\E$";
			pattern = pattern.replace("**", "\\E.*\\Q");
			pattern = pattern.replace("\\Q\\E", "");
		}
		
		warmup = (int)(config.getDouble("warmup", 0) * 1000);
		warmupMessage = config.getString("warmup-message", "");
		interruptWarmupOnDamage = config.getBoolean("warmup-interrupt-on-damage", true);
		warmupInterruptMessage = config.getString("warmup-interrupt-message", "");
		
		cooldown = (int)(config.getDouble("cooldown", 0) * 1000);
		if (cooldown > 0) {
			cooldowns = new HashMap<String, Long>();
		}
		cooldownMessage = config.getString("cooldown-message", "");
		if (cooldownMessage.contains("%hours")) {
			cooldownMessageHours = true;
		} else {
			cooldownMessageHours = false;
		}
		if (cooldownMessage.contains("%minutes")) {
			cooldownMessageMinutes = true;
		} else {
			cooldownMessageMinutes = false;
		}
	}
	
	public boolean handleCommand(SimpleCooldowns plugin, Player player, String msg) {		
		if (player.hasPermission("simplecooldowns.ignore.*") || player.hasPermission("simplecooldowns.ignore." + name)) {
			return false;
		} else if (usePermission && !player.hasPermission("simplecooldowns.use.*") && !player.hasPermission("simplecooldowns.use." + name)) {
			return false;
		}
		if (onCooldown(player)) {
			plugin.sendMessage(player, getCooldownMessage(player));
			return true;
		} else if (warmup > 0) {
			plugin.sendMessage(player, getWarmupMessage());
			plugin.startWarmup(player, this, msg);
			return true;
		} else {
			startCooldown(player);
			return false;
		}
	}
	
	public boolean matches(String command) {
		if (caseSensitive) {
			return command.matches(pattern);
		} else {
			return command.toLowerCase().matches(pattern);
		}
	}
	
	public int getWarmup() {
		return warmup;
	}
	
	public boolean cancelWarmupOnDamage() {
		return interruptWarmupOnDamage;
	}
	
	public String getWarmupInterruptMessage() {
		return warmupInterruptMessage;
	}
	
	public void startCooldown(Player player) {
		if (cooldowns != null) {
			cooldowns.put(player.getName().toLowerCase(), System.currentTimeMillis() + cooldown);
		}
	}
	
	public boolean onCooldown(Player player) {
		if (cooldowns == null) {
			return false;
		}
		
		String name = player.getName().toLowerCase();
		Long cooldown = cooldowns.get(name);
		if (cooldown == null) {
			return false;
		} else if (cooldown > System.currentTimeMillis()) {
			return true;
		} else {
			cooldowns.remove(name);
			return false;
		}
	}
	
	public String getWarmupMessage() {
		return warmupMessage;
	}
	
	public String getCooldownMessage(Player player) {
		if (cooldowns == null) {
			return null;
		}
		
		Long cooldown = cooldowns.get(player.getName().toLowerCase());
		if (cooldown == null) {
			return null;
		}
		
		int seconds = (int)((cooldown - System.currentTimeMillis()) / 1000);
		int minutes = 0;
		int hours = 0;
		
		if (cooldownMessageHours) {
			hours = seconds / (60*60);
			seconds = seconds - (hours*60*60);
		}
		
		if (cooldownMessageMinutes) {
			minutes = seconds / 60;
			seconds = seconds - (minutes*60);
		}
		
		return cooldownMessage.replace("%hours", hours+"").replace("%minutes", minutes+"").replace("%seconds", seconds+"");
	}
	
	public void save(Configuration config) {
		if (cooldowns != null) {
			for (String p : cooldowns.keySet()) {
				long cd = cooldowns.get(p);
				if (cd > System.currentTimeMillis()) {
					config.set(name + "." + p, cd);
				}
			}
		}
	}
	
	public void load(Configuration config) {
		if (cooldowns != null) {
			ConfigurationSection cs = config.getConfigurationSection(name);
			if (cs != null) {
				for (String p : cs.getKeys(false)) {
					long cd = cs.getLong(p);
					if (cd > System.currentTimeMillis()) {
						cooldowns.put(p, cd);
					}
				}
			}
		}
	}
	
}
