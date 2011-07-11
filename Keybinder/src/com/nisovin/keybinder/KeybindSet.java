package com.nisovin.keybinder;

import java.io.File;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkitcontrib.keyboard.Keyboard;
import org.bukkitcontrib.player.ContribPlayer;

public class KeybindSet {

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
	
	public void onKeyPress(Keyboard key) {
		if (keyBindings.containsKey(key)) {
			keyBindings.get(key).onPostKeyPress((ContribPlayer)player);
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
