package com.nisovin.keybinder;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkitcontrib.keyboard.Keyboard;
import org.bukkitcontrib.player.ContribPlayer;

public class KeybindSet {

	private static final int PAGE_SIZE = 6;
	
	private HashMap<Keyboard,Keybind> keyBindings = new HashMap<Keyboard,Keybind>();
	private Player player;
	
	public KeybindSet(Player player) {
		this.player = player;
		Configuration config = new Configuration(new File(Keybinder.plugin.getDataFolder(), player.getName().toLowerCase() + ".yml"));
		config.load();
		
		for (String s : config.getKeys()) {
			Keyboard key = Keyboard.valueOf(s);
			if (key != null) {
				String comm = config.getString(s);
				Keybind keybind = new Keybind(key, comm);
				keyBindings.put(key, keybind);
			}
		}
	}
	
	public void addKeybind(Keybind keybind) {
		keyBindings.put(keybind.getKey(), keybind);
	}
	
	public boolean isKeyBound(Keyboard key) {
		if (keyBindings.containsKey(key)) {
			return true;
		} else {
			return false;
		}
	}
	
	public void onKeyPress(Keyboard key) {
		if (keyBindings.containsKey(key)) {
			keyBindings.get(key).onPostKeyPress((ContribPlayer)player);
		}
	}
	
	public void showList(int page) {
		if (keyBindings.size() == 0) {
			player.sendMessage("You have no key bindings.");
		} else {
			page--;
			if (page*PAGE_SIZE > keyBindings.size()) {
				page = 0;
			}
			TreeMap<String,String> list = new TreeMap<String,String>();
			for (Entry<Keyboard,Keybind> entry : keyBindings.entrySet()) {
				list.put(entry.getKey().name().replace("KEY_",""), entry.getValue().getCommand());
			}
			@SuppressWarnings("unchecked")
			Entry<String,String>[] entries = list.entrySet().toArray(new Entry[0]);
			player.sendMessage("Key bindings (page " + (page+1) + "/" + ((entries.length-1)/PAGE_SIZE+1) + "):");
			for (int i = page*PAGE_SIZE; i < page*PAGE_SIZE+PAGE_SIZE && i < entries.length; i++) {
				player.sendMessage("   " + entries[i].getKey() + ": /" + entries[i].getValue());
			}
		}
	}
	
	public void unbind(String keyStr) {
		Keyboard key = Keyboard.valueOf("KEY_"+keyStr.toUpperCase());
		if (key != null && keyBindings.containsKey(key)) {
			keyBindings.remove(key);
			save();
			player.sendMessage("Key unbound.");
		} else {
			player.sendMessage("You do not have any such keybind.");
		}
	}
	
	public void save() {
		File file = new File(Keybinder.plugin.getDataFolder(), player.getName().toLowerCase() + ".yml");
		if (file.exists()) {
			file.delete();
		}
		Configuration config = new Configuration(new File(Keybinder.plugin.getDataFolder(), player.getName().toLowerCase() + ".yml"));
		for (Keybind keybind : keyBindings.values()) {
			config.getString(keybind.getKey().name(), keybind.getCommand());
		}
		config.save();
	}
	
}
