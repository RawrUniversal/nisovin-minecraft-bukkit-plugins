package com.nisovin.magicspells.util;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;

public class MagicConfig {

	private YamlConfiguration mainConfig;
	private YamlConfiguration altConfig;
	
	public MagicConfig(File file) {
		try {
			mainConfig = new YamlConfiguration();
			mainConfig.load(file);
			String s = this.getString("general.alt-config", null);
			if (s != null && !s.trim().equals("")) {
				s = s.trim();
				File f = new File(MagicSpells.plugin.getDataFolder(), s);
				if (f.exists()) {
					altConfig = new YamlConfiguration();
					altConfig.load(f);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public int getInt(String path, int def) {
		if (altConfig != null && altConfig.contains(path) && altConfig.isInt(path)) {
			return altConfig.getInt(path);
		} else {
			return mainConfig.getInt(path, def);
		}
	}
	
	public double getDouble(String path, double def) {
		if (altConfig != null && altConfig.contains(path) && altConfig.isDouble(path)) {
			return altConfig.getDouble(path);
		} else {
			return mainConfig.getDouble(path, def);
		}
	}
	
	public boolean getBoolean(String path, boolean def) {
		if (altConfig != null && altConfig.contains(path) && altConfig.isBoolean(path)) {
			return altConfig.getBoolean(path);
		} else {
			return mainConfig.getBoolean(path, def);
		}
	}
	
	public String getString(String path, String def) {
		if (altConfig != null && altConfig.contains(path) && altConfig.isString(path)) {
			return altConfig.getString(path);
		} else {
			return mainConfig.getString(path, def);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getIntList(String path, List<Integer> def) {
		if (altConfig != null && altConfig.contains(path)) {
			return altConfig.getList(path);
		} else {
			return mainConfig.getList(path, def);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path, List<String> def) {
		if (altConfig != null && altConfig.contains(path)) {
			return altConfig.getList(path);
		} else {
			return mainConfig.getList(path, def);
		}
	}
	
	public Set<String> getKeys(String path) {
		if (altConfig.contains(path)) {
			return altConfig.getConfigurationSection(path).getKeys(false);
		} else if (mainConfig.contains(path)) {
			return mainConfig.getConfigurationSection(path).getKeys(false);
		} else {
			return null;
		}
	}
	
	public ConfigurationSection getSection(String path) {
		if (altConfig.contains(path)) {
			return altConfig.getConfigurationSection(path);
		} else if (mainConfig.contains(path)) {
			return mainConfig.getConfigurationSection(path);
		} else {
			return null;
		}
	}
	
	public int getInt(Spell spell, String key, int def) {
		return getInt("spells." + spell.getInternalName() + "." + key, def);
	}
	
	public boolean getBoolean(Spell spell, String key, boolean def) {
		return getBoolean("spells." + spell.getInternalName() + "." + key, def);
	}
	
	public String getString(Spell spell, String key, String def) {
		return getString("spells." + spell.getInternalName() + "." + key, def);
	}
	
	public List<Integer> getIntList(Spell spell, String key, List<Integer> def) {
		return getIntList("spells." + spell.getInternalName() + "." + key, def);
	}
	
	public List<String> getStringList(Spell spell, String key, List<String> def) {
		return getStringList("spells." + spell.getInternalName() + "." + key, def);
	}
	
}
