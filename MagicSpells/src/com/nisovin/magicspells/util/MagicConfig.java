package com.nisovin.magicspells.util;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.YamlConfiguration;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;

public class MagicConfig extends YamlConfiguration {

	private YamlConfiguration altConfig;
	
	public MagicConfig(File file) {
		try {
			this.load(file);
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
	
	@Override
	public int getInt(String path, int def) {
		Object o = null;
		if (altConfig != null) {
			o = altConfig.get(path);
		}
		if (o != null && o instanceof Integer) {
			return (Integer)o;
		} else {
			return super.getInt(path, def);
		}
	}
	
	@Override
	public boolean getBoolean(String path, boolean def) {
		Object o = null;
		if (altConfig != null) {
			o = altConfig.get(path);
		}
		if (o != null && o instanceof Boolean) {
			return (Boolean)o;
		} else {
			return super.getBoolean(path, def);
		}		
	}
	
	@Override
	public String getString(String path, String def) {
		String s = null;
		if (altConfig != null) {
			s = altConfig.getString(path, null);
		}
		if (s != null) {
			return s;
		} else {
			return super.getString(path, def);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Integer> getIntList(String path, List<Integer> def) {
		List<Integer> l = null;
		if (altConfig != null) {
			l = (List<Integer>)altConfig.getList(path, null);
		}
		if (l != null) {
			return l;
		} else {
			return super.getList(path, def);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public List<Boolean> getBooleanList(String path, List<Boolean> def) {
		List<Boolean> l = null;
		if (altConfig != null) {
			l = (List<Boolean>)altConfig.getList(path, null);
		}
		if (l != null) {
			return l;
		} else {
			return (List<Boolean>)super.getList(path, def);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStringList(String path, List<String> def) {
		List<String> l = null;
		if (altConfig != null) {
			l = (List<String>)altConfig.getList(path, null);
		}
		if (l != null) {
			return l;
		} else {
			return (List<String>)super.getList(path, def);
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
	
	public List<Boolean> getBooleanList(Spell spell, String key, List<Boolean> def) {
		return getBooleanList("spells." + spell.getInternalName() + "." + key, def);
	}
	
	public List<String> getStringList(Spell spell, String key, List<String> def) {
		return getStringList("spells." + spell.getInternalName() + "." + key, def);
	}
	
}
