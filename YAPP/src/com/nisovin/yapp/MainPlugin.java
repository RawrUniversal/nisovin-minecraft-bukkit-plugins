package com.nisovin.yapp;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.yapp.menu.Menu;

public class MainPlugin extends JavaPlugin {
	
	public static ChatColor TEXT_COLOR = ChatColor.GOLD;
	public static ChatColor HIGHLIGHT_COLOR = ChatColor.YELLOW;
	public static ChatColor ERROR_COLOR = ChatColor.DARK_RED;
	
	public static MainPlugin yapp;
	
	private static boolean debug = true;
	private boolean updatePlayerList = true;

	private Map<String, Group> groups;
	private Map<String, User> players;
	private Group defaultGroup;
	
	private Map<String, PermissionAttachment> attachments;
	
	@Override
	public void onEnable() {
		yapp = this;
		
		load();
		
		// register commands
		getCommand("yapp").setExecutor(new CommandExec());
		
		// register vault hook
		if (getServer().getPluginManager().isPluginEnabled("Vault")) {
			getServer().getServicesManager().register(net.milkbowl.vault.permission.Permission.class, new VaultService(), this, ServicePriority.Highest);
		}
	}
	
	private void load() {
		// get data folder
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		// get config
		File configFile = new File(folder, "config.txt");
		if (!configFile.exists()) {
			this.saveResource("config.txt", false);
		}
		SimpleConfig config = new SimpleConfig(configFile);
		debug = config.getboolean("general.debug");
		updatePlayerList = config.getboolean("general.update player list");
		boolean modalMenu = config.getboolean("general.modal menu");
		String defGroupName = config.getString("general.default group");
				
		// load all group data
		loadGroups();
		
		// get default group
		if (defGroupName != null && !defGroupName.isEmpty()) {
			defaultGroup = getGroup(defGroupName);
			if (defaultGroup == null) {
				// create default group
				defaultGroup = newGroup(defGroupName);
				defaultGroup.addPermission(null, "yapp.build");
				defaultGroup.save();
				log("Created default group '" + defGroupName + "'");
			}
		}
		
		// load logged in players
		players = new HashMap<String, User>();
		attachments = new HashMap<String, PermissionAttachment>();
		for (Player player : getServer().getOnlinePlayers()) {
			loadPlayerPermissions(player);
		}
		
		// register listeners
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PermListener(this), this);
		if (config.getboolean("general.use build perm")) {
			pm.registerEvents(new BuildListener(), this);
		}
		if (config.getboolean("general.use chat prefixes")) {
			pm.registerEvents(new ChatListener(), this);
		}
		
		// create converation factory
		Menu.initializeFactory(this, modalMenu);
	}
	
	private void unload() {
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
	}
	
	@Override
	public void onDisable() {
		unload();
		
		getServer().getServicesManager().unregisterAll(this);
		Menu.closeAllMenus();
		
		yapp = null;
	}
	
	public void reload() {
		unload();
		load();
	}
	
	public void cleanup() {
		Iterator<Map.Entry<String,User>> iter = players.entrySet().iterator();
		Map.Entry<String,User> entry;
		while (iter.hasNext()) {
			entry = iter.next();
			if (entry.getValue().getPlayer() == null) {
				entry.getValue().save();
				iter.remove();
			}
		}
		
		Iterator<Map.Entry<String,PermissionAttachment>> iter2 = attachments.entrySet().iterator();
		Map.Entry<String,PermissionAttachment> entry2;
		while (iter2.hasNext()) {
			entry2 = iter2.next();
			if (Bukkit.getPlayerExact(entry2.getKey()) == null) {
				entry2.getValue().remove();
				iter2.remove();
			}
		}
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
			if (yapp.defaultGroup != null && user.getGroups(null).size() == 0) {
				user.addGroup(null, yapp.defaultGroup);
				user.save();
				debug("Added default group '" + yapp.defaultGroup.getName() + "' to player '" + playerName + "'");
			}
		}
		return user;
	}
	
	public User loadPlayerPermissions(Player player) {
		String playerName = player.getName().toLowerCase();
		String worldName = player.getWorld().getName();
		debug("Loading player permissions for " + playerName + "...");
		
		// prepare user
		User user = getPlayerUser(playerName);
		user.resetCachedInfo();
		user.getColor(worldName);
		user.getPrefix(worldName);
		
		// prepare attachment
		PermissionAttachment attachment = attachments.get(playerName);
		if (attachment == null) {
			attachment = player.addAttachment(this);
			attachments.put(playerName, attachment);
		}
		attachment.getPermissions().clear();
		
		// load permissions
		debug("  Adding permissions");
		List<PermissionNode> nodes = user.getAllPermissions(worldName);
		for (PermissionNode node : nodes) {
			node.addTo(attachment);
			debug("    Added: " + node);
		}
		player.recalculatePermissions();
		
		// set player list color
		if (updatePlayerList) {
			player.setPlayerListName(user.getColor() + player.getName());
		}
		
		return user;
	}
	
	public void unloadPlayer(Player player) {
		String playerName = player.getName().toLowerCase();
		players.remove(playerName).save();
		attachments.remove(playerName).remove();
	}
	
	public void saveAll() {
		for (User user : players.values()) {
			user.save();
		}
		for (Group group : groups.values()) {
			group.save();
		}
	}
	
	public static Group newGroup(String name) {
		Group group = new Group(name);
		yapp.groups.put(name.toLowerCase(), group);
		return group;
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
