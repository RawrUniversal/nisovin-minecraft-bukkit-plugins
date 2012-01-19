package com.nisovin.worldloader;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class WorldLoader extends JavaPlugin {

	private static final int MAX_INSTANCES = 8;
	
	public static WorldLoader plugin;
	
	protected HashMap<String,WorldBase> worldBases;	
	protected HashMap<String,WorldInstance> loadedWorlds;
	protected HashMap<String,Party> parties;
	protected HashMap<String,String> playerLocations;
	protected HashMap<String,PendingAction> pendingActions;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		// create folders if necessary
		File pluginDir = getDataFolder();
		if (!pluginDir.exists()) {
			pluginDir.mkdir();
		}
		File worldsDir = new File(pluginDir, "worlds");
		if (!worldsDir.exists()) {
			worldsDir.mkdir();
		}
		
		// load world bases
		worldBases = new HashMap<String,WorldBase>();
		Configuration config = getConfig();
		ConfigurationSection worldsSection = config.getConfigurationSection("worlds");
		if (worldsSection != null) {
			Set<String> worldNodes = worldsSection.getKeys(false);
			if (worldNodes != null) {
				for (String key : worldNodes) {
					worldBases.put(key, new WorldBase(key, worldsSection.getConfigurationSection(key)));
				}
			}
		}
		
		loadedWorlds = new HashMap<String,WorldInstance>();
		parties = new HashMap<String, Party>();
		playerLocations = new HashMap<String, String>();
		pendingActions = new HashMap<String, PendingAction>();
		
		// register commands and events
		CommandExec exec = new CommandExec(this);
		
		getCommand("loadworld").setExecutor(exec);
		getCommand("saveworld").setExecutor(exec);
		getCommand("newworld").setExecutor(exec);
		
		getCommand("start").setExecutor(exec);
		getCommand("minplayers").setExecutor(exec);
		getCommand("maxplayers").setExecutor(exec);
		getCommand("breakable").setExecutor(exec);
		getCommand("placeable").setExecutor(exec);
		getCommand("monsters").setExecutor(exec);
		
		getCommand("party").setExecutor(exec);
		getCommand("invite").setExecutor(exec);
		getCommand("leave").setExecutor(exec);
		
		getServer().getPluginManager().registerEvents(new EventListener(this), this);
	}

	public void addPendingAction(PendingAction action) {
		pendingActions.put(action.getPlayer().getName().toLowerCase(), action);
	}
	
	public boolean acceptPendingAction(Player player) {
		PendingAction action = pendingActions.get(player.getName().toLowerCase());
		if (action != null) {
			action.execute();
			pendingActions.remove(player.getName().toLowerCase());
			return true;
		} else {
			return false;
		}
	}
	
	public boolean rejectPendingAction(Player player) {
		PendingAction r = pendingActions.remove(player.getName().toLowerCase());
		return (r != null);
	}
	
	public void addPlayerToParty(Player player, Party party) {
		party.add(player);
		parties.put(player.getName().toLowerCase(), party);
	}
	
	public WorldInstance launchInstance(WorldBase base, Party party) {
		if (!base.canPartyJoin(party)) {
			party.getLeader().sendMessage("Your party is not the correct size to enter that instance.");
			return null;
		}
		
		WorldInstance instance = launchInstance(base, false);
		if (instance == null) {
			party.getLeader().sendMessage("Unable to launch instance.");
			return null;
		}
		
		for (String s : party.getMembers()) {
			Player p = Bukkit.getPlayerExact(s);
			if (p != null) {
				instance.teleport(p);
			}
		}
		
		return instance;
	}
	
	public WorldInstance launchInstance(WorldBase base, boolean allowWorldGen) {
		if (loadedWorlds.size() > MAX_INSTANCES) {
			return null;
		}
		
		WorldInstance instance = new WorldInstance(base, allowWorldGen);
		loadedWorlds.put(instance.getWorldName(), instance);
		return instance;
	}
	
	public WorldInstance launchInstance(WorldBase base, String instanceName) {
		if (loadedWorlds.size() > MAX_INSTANCES) {
			return null;
		}
		
		WorldInstance instance = new WorldInstance(base, instanceName);
		loadedWorlds.put(instanceName, instance);
		return instance;
	}
	
	public void killInstance(WorldInstance instance) {
		System.out.println("Killing empty instance: " + instance.getWorldName());
		instance.unloadWorld();
		loadedWorlds.remove(instance.getWorldName());
	}
	
	public void enterSavedInstance(final Player player) {
		String s = playerLocations.get(player.getName().toLowerCase());
		if (s == null) {
			return;
		}
		
		if (loadedWorlds.containsKey(s)) {
			loadedWorlds.get(s).teleport(player);
		} else {
			String baseName = s.split("-")[0];
			WorldBase base = worldBases.get(baseName);
			if (base != null) {
				final WorldInstance instance = launchInstance(base, s);
				System.out.println("Loading saved instance: " + instance.getWorldName());
				if (instance != null && instance.worldLoaded()) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							instance.teleport(player);
						}
					}, 2);
				}
			}
		}
	}
	
	public WorldInstance getWorldInstance(Player player) {
		return getWorldInstance(player.getWorld());
	}
	
	public WorldInstance getWorldInstance(World world) {
		return loadedWorlds.get(world.getName());
	}

	@Override
	public void onDisable() {
		for (WorldInstance world : loadedWorlds.values()) {
			world.unloadWorld();
		}
		loadedWorlds.clear();
	}

}
