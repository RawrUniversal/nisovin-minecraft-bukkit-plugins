package com.nisovin.yapp;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class MainPlugin extends JavaPlugin {
	
	public static MainPlugin yapp;
	public static boolean debug = true;

	private Map<String, Group> groups;
	private Map<String, User> players;
	
	private Map<String, PermissionAttachment> attachments;
	
	@Override
	public void onEnable() {
		yapp = this;
		
		// get data folder
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		// load all group data
		loadGroups();
		
		
		// load logged in players
		players = new HashMap<String, User>();
		attachments = new HashMap<String, PermissionAttachment>();
		for (Player player : getServer().getOnlinePlayers()) {
			loadPlayerPermissions(player);
		}
		
		// register listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PermListener(this), this);
		
		// register vault hook
		if (pm.isPluginEnabled("Vault")) {
			getServer().getServicesManager().register(net.milkbowl.vault.permission.Permission.class, new VaultService(), this, ServicePriority.Highest);
		}
	}
	
	@Override
	public void onDisable() {
		saveAll();
		
		groups.clear();
		groups = null;
		players.clear();
		players = null;
		
		for (PermissionAttachment attachment : attachments.values()) {
			attachment.remove();
		}
		attachments.clear();
		attachments = null;
		
		HandlerList.unregisterAll(this);
		getServer().getServicesManager().unregisterAll(this);
		
		yapp = null;
	}
	
	public void reload() {
		onDisable();
		onEnable();
	}
	
	public void loadGroups() {
		debug("Loading groups...");
		groups = new TreeMap<String, Group>();
		
		// get groups from group folder
		File groupsFolder = new File(getDataFolder(), "groups");
		if (groupsFolder.exists() && groupsFolder.isDirectory()) {
			File[] groupFiles = groupsFolder.listFiles();
			for (File f : groupFiles) {
				if (f.getName().endsWith(".txt")) {
					String name = f.getName().replace(".txt", "");
					if (!groups.containsKey(name)) {
						Group group = new Group(name);
						groups.put(name, group);
						debug("  Found group: " + name);
					}
				}
			}
		}
		
		// get groups from world group folders
		File worldsFolder = new File(getDataFolder(), "worlds");
		if (worldsFolder.exists() && worldsFolder.isDirectory()) {
			File[] worldFolders = worldsFolder.listFiles();
			for (File wf : worldFolders) {
				if (wf.isDirectory()) {
					File worldGroupsFolder = new File(wf, "groups");
					if (worldGroupsFolder.exists() && worldGroupsFolder.isDirectory()) {
						File[] groupFiles = worldGroupsFolder.listFiles();
						for (File f : groupFiles) {
							if (f.getName().endsWith(".txt")) {
								String name = f.getName().replace(".txt", "");
								if (!groups.containsKey(name)) {
									Group group = new Group(name);
									groups.put(name, group);
									debug("  Found group: " + name);
								}
							}
						}
					}
				}
			}
		}
		
		// load group data
		for (Group group : groups.values()) {
			group.loadFromFiles();
		}
	}
	
	public static User getPlayerUser(String playerName) {
		User user = yapp.players.get(playerName);
		if (user == null) {
			user = new User(playerName);
			yapp.players.put(playerName, user);
			user.loadFromFiles();
		}
		return user;
	}
	
	public void loadPlayerPermissions(Player player) {
		String playerName = player.getName().toLowerCase();
		debug("Loading player permissions for " + playerName + "...");
		
		// prepare user
		User user = getPlayerUser(playerName);
		
		// prepare attachment
		PermissionAttachment attachment = attachments.get(playerName);
		if (attachment == null) {
			attachment = player.addAttachment(this);
			attachments.put(playerName, attachment);
		}
		attachment.getPermissions().clear();
		
		// load permissions
		debug("  Adding permissions");
		List<PermissionNode> nodes = user.getAllPermissions(player.getWorld());
		for (PermissionNode node : nodes) {
			node.addTo(attachment);
			debug("    Added: " + node);
		}
		player.recalculatePermissions();
	}
	
	public void unloadPlayer(Player player) {
		String playerName = player.getName().toLowerCase();
		players.remove(playerName).save();
		attachments.remove(playerName);
	}
	
	public void saveAll() {
		for (User user : players.values()) {
			user.save();
		}
		for (Group group : groups.values()) {
			group.save();
		}
	}
	
	public static void addGroup(Group group) {
		yapp.groups.put(group.getName().toLowerCase(), group);
	}
	
	public static Group getGroup(String name) {
		return yapp.groups.get(name.toLowerCase());
	}
	
	public static Set<String> getGroupNames() {
		return yapp.groups.keySet();
	}
	
	public static void log(String message) {
		yapp.getLogger().info(message);
	}
	
	public static void warning(String message) {
		yapp.getLogger().warning(message);
	}
	
	public static void error(String message) {
		yapp.getLogger().severe(message);
	}
	
	public static void debug(String message) {
		if (debug) {
			yapp.getLogger().info(message);
		}
	}
	
}
