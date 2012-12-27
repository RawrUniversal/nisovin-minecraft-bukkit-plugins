package com.nisovin.yapp;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.yapp.denyperms.*;
import com.nisovin.yapp.menu.Menu;

public class MainPlugin extends JavaPlugin {
	
	public static ChatColor TEXT_COLOR = ChatColor.GOLD;
	public static ChatColor HIGHLIGHT_COLOR = ChatColor.YELLOW;
	public static ChatColor ERROR_COLOR = ChatColor.DARK_RED;
	
	public static MainPlugin yapp;
	public static long mainThreadId;
	
	private static boolean debug = true;
	private boolean updateDisplayName = true;
	private boolean updatePlayerList = true;
	private boolean setPlayerGroupPerm = false;
	private boolean setPlayerMetadata = false;

	private Map<String, Group> groups;
	private Map<String, User> players;
	private Group defaultGroup;
	private Map<String, List<Group>> ladders;
	
	private Map<String, PermissionAttachment> attachments;
	
	@Override
	public void onEnable() {
		yapp = this;
		mainThreadId = Thread.currentThread().getId();
		
		load();
		
		// register commands
		getCommand("yapp").setExecutor(new CommandMain());
		CommandPromoteDemote cpd = new CommandPromoteDemote(this);
		getCommand("yapppromote").setExecutor(cpd);
		getCommand("yappdemote").setExecutor(cpd);
		getCommand("yappconvert").setExecutor(new CommandConvert());
		
		// register vault hook
		if (getServer().getPluginManager().isPluginEnabled("Vault")) {
			getServer().getServicesManager().register(net.milkbowl.vault.permission.Permission.class, new VaultService(), this, ServicePriority.Highest);
			getLogger().info("Vault hooked");
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
		updateDisplayName = config.getboolean("general.update display name");
		updatePlayerList = config.getboolean("general.update player list");
		setPlayerGroupPerm = config.getboolean("general.set group perm");
		setPlayerMetadata = config.getboolean("general.set player metadata");
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
		
		// get promotion ladders
		ladders = new LinkedHashMap<String, List<Group>>();
		Set<String> keys = config.getKeys("ladders");
		if (keys != null) {
			for (String key : keys) {
				List<String> groupList = config.getStringList("ladders." + key);
				List<Group> ladderGroups = new ArrayList<Group>();
				for (String s : groupList) {
					Group g = getGroup(s);
					if (g == null) {
						g = newGroup(s);
					}
					ladderGroups.add(g);
				}
				if (ladderGroups != null) {
					ladders.put(key, ladderGroups);
				}
			}
		}
		
		// load logged in players
		players = Collections.synchronizedMap(new HashMap<String, User>());
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
		if (config.getboolean("general.use chat formatting")) {
			pm.registerEvents(new ChatListener(), this);
		}
		if (config.getboolean("general.set nameplate color") && getServer().getPluginManager().isPluginEnabled("TagAPI")) {
			pm.registerEvents(new NameplateListener(), this);
		}
		
		// register deny perms
		if (config.getboolean("deny permissions.place")) {
			pm.registerEvents(new PlaceListener(), this);
		}
		if (config.getboolean("deny permissions.break")) {
			pm.registerEvents(new BreakListener(), this);
		}
		if (config.getboolean("deny permissions.craft")) {
			pm.registerEvents(new CraftListener(), this);
		}
		if (config.getboolean("deny permissions.pickup")) {
			pm.registerEvents(new PickupListener(), this);
		}
		if (config.getboolean("deny permissions.drop")) {
			pm.registerEvents(new DropListener(), this);
		}
		if (config.getboolean("deny permissions.useitem") || config.getboolean("deny permissions.useblock")) {
			pm.registerEvents(new UseListener(config.getboolean("deny permissions.useitem"), config.getboolean("deny permissions.useblock")), this);
		}
		if (config.getboolean("deny permissions.interact")) {
			pm.registerEvents(new InteractListener(), this);
		}
		if (config.getboolean("deny permissions.targeted")) {
			pm.registerEvents(new TargetListener(), this);
		}
		if (config.getboolean("deny permissions.attack") || config.getboolean("deny permissions.damage")) {
			pm.registerEvents(new DamageListener(config.getboolean("deny permissions.attack"), config.getboolean("deny permissions.damage")), this);
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
					if (!groups.containsKey(name.toLowerCase())) {
						Group group = new Group(name);
						groups.put(name.toLowerCase(), group);
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
								String name = f.getName().replace(".txt", "").toLowerCase();
								if (!groups.containsKey(name.toLowerCase())) {
									Group group = new Group(name);
									groups.put(name.toLowerCase(), group);
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
		User user = yapp.players.get(playerName.toLowerCase());
		if (user == null) {
			user = new User(playerName);
			yapp.players.put(playerName.toLowerCase(), user);
			user.loadFromFiles();
			if (yapp.defaultGroup != null && user.getGroups(null).size() == 0) {
				user.addGroup(null, yapp.defaultGroup);
				debug("Added default group '" + yapp.defaultGroup.getName() + "' to player '" + playerName + "'");
				user.save();
			}
		}
		return user;
	}
	
	@SuppressWarnings("unchecked")
	public User loadPlayerPermissions(Player player) {
		long start = System.nanoTime();
		
		String playerName = player.getName().toLowerCase();
		String worldName = player.getWorld().getName();
		debug("Loading player permissions for " + playerName + "...");
		
		// prepare user
		User user = getPlayerUser(playerName);
		user.clearCache(true);
		user.save();
		user.getColor(worldName);
		user.getPrefix(worldName);
		Group primaryGroup = user.getPrimaryGroup(player.getWorld().getName());
		
		// prepare attachment
		PermissionAttachment attachment = attachments.get(playerName);
		if (attachment == null) {
			attachment = player.addAttachment(this);
			attachments.put(playerName, attachment);
		}
		Map<String, Boolean> permissions;
		try {
			Field mapField = PermissionAttachment.class.getDeclaredField("permissions");
			mapField.setAccessible(true);
			permissions = (Map<String, Boolean>)mapField.get(attachment);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// load permissions
		permissions.clear();
		debug("  Adding permissions");
		if (setPlayerGroupPerm && primaryGroup != null) {
			permissions.put("group." + primaryGroup.getName(), true);
		}
		List<PermissionNode> nodes = user.getAllPermissions(worldName);
		for (PermissionNode node : nodes) {
			permissions.put(node.getNodeName(), node.getValue());
			debug("    Added: " + node);
		}
		player.recalculatePermissions();
		
		// set display name
		if (updateDisplayName) {
			player.setDisplayName(user.getColor(worldName) + player.getName());
		}
		
		// set player list color
		setPlayerListName(player, user);
		
		// set metadata
		if (setPlayerMetadata) {
			if (primaryGroup != null) {
				player.removeMetadata("group", this);
				player.setMetadata("group", new FixedMetadataValue(this, primaryGroup.getName()));
			}
		}
		
		if (debug) {
			long elapsed = System.nanoTime() - start;
			debug("  Elapsed time: " + (elapsed / 1000000F) + "ms");
		}
		
		return user;
	}
	
	private void loadAllUsers() {
		File playersFolder = new File(getDataFolder(), "players");
		String fileName, playerName;
		for (File file : playersFolder.listFiles()) {
			fileName = file.getName().toLowerCase();
			if (fileName.endsWith(".txt")) {
				playerName = fileName.replace(".txt", "");
				User user = getPlayerUser(playerName);
				players.put(playerName, user);
			}
		}
	}
	
	public void renameOrDeleteGroup(Group group, String newName) {
		// create new group as copy of old
		Group newGroup = null;
		if (newName != null && !newName.isEmpty()) {
			newGroup = new Group(group, newName);
			newGroup.save();
		}
		
		// replace group in groups
		for (Group g : groups.values()) {
			g.replaceGroup(group, newGroup);
		}
		
		// replace group in players
		loadAllUsers();
		for (User u : players.values()) {
			u.replaceGroup(group, newGroup);
		}
		
		// save and clean up
		saveAll();
		cleanup();
		
		// remove old group
		String oldName = group.getName();
		groups.remove(oldName.toLowerCase());
		File file = new File(getDataFolder(), "groups" + File.separator + oldName + ".txt");
		if (file.exists()) {
			file.delete();
		}
		File worldsFolder = new File(getDataFolder(), "worlds");
		if (worldsFolder.exists()) {
			for (File f : worldsFolder.listFiles()) {
				if (f.isDirectory()) {
					file = new File(f, oldName + ".txt");
					if (file.exists()) {
						file.delete();
					}
				}
			}
		}
		
		// finally add new group
		if (newGroup != null) {
			groups.put(newName.toLowerCase(), newGroup);
		}
		
		reload();
	}
	
	public boolean promote(User user, String world, CommandSender sender) {
		List<Group> groups;
		if (world == null) {
			groups = user.getActualGroupList();
		} else {
			groups = user.getActualGroupList(world);
		}
		if (groups == null || groups.size() == 0) {
			return false;
		} else {
			Group group = groups.get(0);
			for (String ladderName : ladders.keySet()) {
				if (sender.hasPermission("yapp.promote.*") || sender.hasPermission("yapp.promote." + ladderName)) {
					List<Group> ladder = ladders.get(ladderName);
					int index = ladder.indexOf(group) + 1;
					if (index > 0 && index < ladder.size()) {
						user.replaceGroup(group, ladder.get(index));
						return true;
					}
				}
			}
			return false;
		}
	}
	
	public boolean demote(User user, String world, CommandSender sender) {
		List<Group> groups;
		if (world == null) {
			groups = user.getActualGroupList();
		} else {
			groups = user.getActualGroupList(world);
		}
		if (groups == null || groups.size() == 0) {
			return false;
		} else {
			Group group = groups.get(0);
			for (String ladderName : ladders.keySet()) {
				if (sender.hasPermission("yapp.demote.*") || sender.hasPermission("yapp.demote." + ladderName)) {
					List<Group> ladder = ladders.get(ladderName);
					int index = ladder.indexOf(group) - 1;
					if (index >= 0) {
						user.replaceGroup(group, ladder.get(index));
						return true;
					}
				}
			}
			return false;
		}
	}
		
	public void setPlayerListName(Player player, User user) {
		if (updatePlayerList) {
			String world = player.getWorld().getName();
			String name = user.getColor(world) + player.getName();
			if (name.length() > 15) {
				name = name.substring(0, 15);
			}
			player.setPlayerListName(name);
		}		
	}
	
	public void unloadPlayer(Player player) {
		String playerName = player.getName().toLowerCase();
		players.remove(playerName).save();
		attachments.remove(playerName).remove();
	}
	
	public void unloadPlayer(String playerName) {
		playerName = playerName.toLowerCase();
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
	
	public static Group getDefaultGroup() {
		return yapp.defaultGroup;
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
