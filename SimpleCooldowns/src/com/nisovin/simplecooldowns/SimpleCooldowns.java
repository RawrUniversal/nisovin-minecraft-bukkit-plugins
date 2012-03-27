package com.nisovin.simplecooldowns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SimpleCooldowns extends JavaPlugin {

	public static SimpleCooldowns plugin;
		
	private ArrayList<Command> commands = new ArrayList<Command>();
	private HashMap<String,HashSet<Warmup>> warmups = new HashMap<String, HashSet<Warmup>>();

	public Command findCommand(String command) {
		for (Command c : commands) {
			if (c.matches(command)) {
				return c;
			}
		}
		return null;
	}
	
	public void startWarmup(Player player, Command command, String msg) {
		String name = player.getName().toLowerCase();
		HashSet<Warmup> w = warmups.get(name);
		if (w == null) {
			w = new HashSet<Warmup>();
			warmups.put(name, w);
		}
		w.add(new Warmup(player, command, msg));
	}
	
	public void removeWarmup(Player player, Warmup warmup) {
		HashSet<Warmup> w = warmups.get(player.getName().toLowerCase());
		if (w != null) {
			w.remove(warmup);
			if (w.size() == 0) {
				warmups.remove(player.getName().toLowerCase());
			}
		}
	}
	
	public void interruptWarmups(Player player, String source) {
		HashSet<Warmup> w = warmups.get(player.getName().toLowerCase());
		if (w != null) {
			Iterator<Warmup> iter = w.iterator();
			while (iter.hasNext()) {
				Warmup warmup = iter.next();
				boolean interrupted = warmup.interrupt(source);
				if (interrupted) {
					iter.remove();
				}
			}
			if (w.size() == 0) {
				warmups.remove(player.getName().toLowerCase());
			}
		}
	}
	
	public void sendMessage(Player player, String message) {
		if (message != null && !message.equals("")) {
			String [] msgs = message.replaceAll("&([0-9a-f])", "\u00A7$1").split("\n");
			for (String msg : msgs) {
				if (!msg.equals("")) {
					player.sendMessage(msg);
				}
			}
		}
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		loadConfig();
		loadCooldowns();
		
		getServer().getPluginManager().registerEvents(new CooldownListener(this), this);
	}

	@Override
	public void onDisable() {
		saveCooldowns();
	}
	
	private void loadConfig() {
		Configuration config = getConfig();
		
		// load commands
		commands.clear();
		ConfigurationSection commandSection = config.getConfigurationSection("commands");
		Set<String> keys = commandSection.getKeys(false);
		for (String key : keys) {
			commands.add(new Command(key, commandSection.getConfigurationSection(key)));
		}
	}
	
	private void loadCooldowns() {
		File file = new File(getDataFolder(), "save.yml");
		if (file.exists()) {
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
				for (Command command : commands) {
					command.load(config);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveCooldowns() {
		File file = new File(getDataFolder(), "save.yml");
		if (file.exists()) {
			file.delete();
		}
		YamlConfiguration config = new YamlConfiguration();
		for (Command command : commands) {
			command.save(config);
		}
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}
