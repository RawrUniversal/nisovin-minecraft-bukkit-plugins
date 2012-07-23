package com.nisovin.codelock;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class CodeLock extends JavaPlugin implements Listener {

	private HashMap<String, String> locks = new HashMap<String, String>();
		
	@Override
	public void onEnable() {		
		// load config
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			this.saveDefaultConfig();
		}
		Configuration config = getConfig();
		
		Settings.lockInventorySize = config.getInt("lock-inventory-size", Settings.lockInventorySize);
		Settings.lockTitle = config.getString("lock-title", Settings.lockTitle);
		if (config.contains("buttons")) {
			List<Integer> list = config.getIntegerList("buttons");
			Settings.buttons = new Material[list.size()];
			for (int i = 0; i < Settings.buttons.length; i++) {
				Settings.buttons[i] = Material.getMaterial(list.get(i).intValue());
			}
		}
		if (config.contains("button-positions")) {
			List<Integer> list = config.getIntegerList("button-positions");
			Settings.buttonPositions = new int[list.size()];
			for (int i = 0; i < Settings.buttonPositions.length; i++) {
				Settings.buttonPositions[i] = list.get(i).intValue();
			}
		}
		if (config.contains("letter-codes")) {
			List<String> list = config.getStringList("letter-codes");
			Settings.letterCodes = new char[list.size()];
			for (int i = 0; i < Settings.letterCodes.length; i++) {
				Settings.letterCodes[i] = list.get(i).charAt(0);
			}
		}
		Settings.autoDoorClose = config.getInt("aut-door-close", Settings.autoDoorClose);
		Settings.checkBuildPerms = config.getBoolean("check-build-perms", Settings.checkBuildPerms);		
		Settings.lockable.clear();
		List<String> list = config.getStringList("lockable");
		if (list != null) {
			for (String s : list) {
				Material mat = Material.getMaterial(s.toUpperCase());
				if (mat != null) {
					Settings.lockable.add(mat);
				}
			}
		}		
		Settings.strLocked = config.getString("str-locked", Settings.strLocked);
		Settings.strRemoved = config.getString("str-removed", Settings.strRemoved);
		
		// load locks
		load();
		
		// register events
		getServer().getPluginManager().registerEvents(new LockListener(this), this);
		getServer().getPluginManager().registerEvents(new ProtectListener(this), this);
	}
	
	public boolean isLocked(Block block) {
		return locks.containsKey(Utilities.getLocStr(block));
	}
	
	public String getCode(Block block) {
		return locks.get(Utilities.getLocStr(block));
	}
	
	public void addLock(Block block, String code) {
		Material type = block.getType();
		locks.put(Utilities.getLocStr(block), code);
		if (type == Material.CHEST) {
			int[] xoff = new int[] {-1, 0, 1, 0};
			int[] zoff = new int[] {0, -1, 0, 1};
			for (int i = 0; i < 4; i++) {
				if (block.getRelative(xoff[i], 0, zoff[i]).getType() == Material.CHEST) {
					locks.put(Utilities.getLocStr(block, xoff[i], 0 , zoff[i]), code);
				}
			}
		} else if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK) {
			if (block.getRelative(BlockFace.UP).getType() == type) {
				locks.put(Utilities.getLocStr(block, 0, 1, 0), code);
			} else if (block.getRelative(BlockFace.DOWN).getType() == type) {
				locks.put(Utilities.getLocStr(block, 0, -1, 0), code);
			}
		}
		save();
	}
	
	public void removeLock(Block block) {
		locks.remove(Utilities.getLocStr(block));
		if (block.getType() == Material.CHEST) {
			locks.remove(Utilities.getLocStr(block, -1, 0, 0));
			locks.remove(Utilities.getLocStr(block, 1, 0, 0));
			locks.remove(Utilities.getLocStr(block, 0, 0, 1));
			locks.remove(Utilities.getLocStr(block, 0, 0, -1));
		} else if (block.getType() == Material.WOODEN_DOOR || block.getType() == Material.IRON_DOOR_BLOCK) {
			locks.remove(Utilities.getLocStr(block, 0, 1, 0));
			locks.remove(Utilities.getLocStr(block, 0, -1, 0));
		}
		save();
	}
	
	private void load() {
		File file = new File(getDataFolder(), "data.yml");
		if (file.exists()) {
			YamlConfiguration yaml = new YamlConfiguration();
			try {
				yaml.load(file);
				for (String s : yaml.getKeys(false)) {
					locks.put(s, yaml.getString(s));
				}
			} catch (Exception e) {
				getLogger().severe("Failed to load data!");
				e.printStackTrace();
				this.setEnabled(false);
			}
		}
	}
	
	private void save() {
		File file = new File(getDataFolder(), "data.yml");
		if (file.exists()) {
			file.delete();
		}
		YamlConfiguration yaml = new YamlConfiguration();
		try {
			for (String s : locks.keySet()) {
				yaml.set(s, locks.get(s));
			}
			yaml.save(file);
		} catch (Exception e) {
			getLogger().severe("Failed to save data!");
			e.printStackTrace();
		}
	}
	
}
