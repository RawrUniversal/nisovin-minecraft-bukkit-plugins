package com.nisovin.yapp;

import java.io.File;
import java.io.IOException;
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
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.yapp.denyperms.*;
import com.nisovin.yapp.menu.Menu;
import com.nisovin.yapp.storage.*;
import com.nisovin.yapp.vault.VaultHandler;

public class YAPP extends JavaPlugin {
	
	public static ChatColor TEXT_COLOR = ChatColor.GOLD;
	public static ChatColor HIGHLIGHT_COLOR = ChatColor.YELLOW;
	public static ChatColor ERROR_COLOR = ChatColor.DARK_RED;
	
	public static YAPP plugin;
	public static long mainThreadId;
	
	private static boolean debug = true;
	private boolean updateDisplayName = true;
	private String displayNameFormat = null;
	private boolean updatePlayerList = true;
	private boolean setPlayerGroupPerm = false;
	private boolean setPlayerMetadata = false;

	private Map<String, Group> groups;
	private Map<String, User> players;
	private Group defaultGroup;
	private Map<String, List<Group>> ladders;
	
	private Field permissionMapField;
	private Map<String, PermissionAttachment> attachments;
	
	private StorageMethod storageMethod;
	
	@Override
	public void onEnable() {
		plugin = this;
		mainThreadId = Thread.currentThread().getId();

		// access permission map field, for quick permission modification
		try {
			permissionMapField = PermissionAttachment.class.getDeclaredField("permissions");
			permissionMapField.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
			this.setEnabled(false);
			return;
		}
		
		// load plugin
		load();
		
		// register commands
		getCommand("yapp").setExecutor(new CommandMain());
		CommandPromoteDemote cpd = new CommandPromoteDemote(this);
		getCommand("yapppromote").setExecutor(cpd);
		getCommand("yappdemote").setExecutor(cpd);
		getCommand("yappconvert").setExecutor(new CommandConvert());
		
		// register vault hook
		if (getServer().getPluginManager().isPluginEnabled("Vault")) {
			VaultHandler.registerHooks(this);
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
		displayNameFormat = config.getString("general.display name format");
		updatePlayerList = config.getboolean("general.update player list");
		setPlayerGroupPerm = config.getboolean("general.set group perm");
		setPlayerMetadata = config.getboolean("general.set player metadata");
		boolean modalMenu = config.getboolean("general.modal menu");
		String defGroupName = config.getString("general.default group");
		
		// get storage method
		if (config.getboolean("mysql.enabled")) {
			try {
				storageMethod = new MySQLStorage(config.getString("mysql.host"), config.getString("mysql.user"), config.getString("mysql.pass"), config.getString("mysql.db"));
			} catch (IOException e) {
				error("UNABLE TO ACCESS MYSQL PERMISSION DATABASE. USING FILE SYSTEM INSTEAD.");
				e.printStackTrace();
				storageMethod = new FileStorage();
			}
		} else {
			storageMethod = new FileStorage();
		}
		
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
						g.save();
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
			pm.registerEvents(new ChatListener(config.getString("general.chat format")), this);
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
		
		plugin = null;
	}
	
	public void reload() {
		unload();
		load();
	}
	
	private void cleanup() {
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
	
	private void loadGroups() {
		debug("Loading groups...");
		groups = new TreeMap<String, Group>();		
		storageMethod.fillGroupMap(groups);
		
		// load group data
		for (Group group : groups.values()) {
			group.load();
		}
	}
	
	/**
	 * Gets the User object for a specific player. This should never return null.
	 * @param playerName The name of the player.
	 * @return A User object for the player.
	 */
	public static User getPlayerUser(String playerName) {
		User user = plugin.players.get(playerName.toLowerCase());
		if (user == null) {
			user = new User(playerName);
			plugin.players.put(playerName.toLowerCase(), user);
			user.load();
			if (plugin.defaultGroup != null && user.getGroups(null).size() == 0) {
				user.addGroup(null, plugin.defaultGroup);
				debug("Added default group '" + plugin.defaultGroup.getName() + "' to player '" + playerName + "'");
				user.save();
			}
		}
		return user;
	}
	
	protected boolean hasLoadedPermissions(Player player) {
		return attachments.containsKey(player.getName().toLowerCase());
	}
	
	/**
	 * Loads the specified player's permissions into the Player object, so that it can be checked
	 * via Player.hasPermission(String). This is called when the player joins the game and when
	 * the player changes worlds.
	 * @param player The player to load permissions for.
	 * @return The User object for the player.
	 */
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
			permissions = (Map<String, Boolean>)permissionMapField.get(attachment);
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
			if (displayNameFormat == null || displayNameFormat.isEmpty()) {
				player.setDisplayName(user.getColor(worldName) + player.getName());
			} else {
				player.setDisplayName(displayNameFormat
						.replace("%name%", player.getName())
						.replace("%color%", user.getColor(worldName).toString())
						.replace("%prefix%", user.getPrefix(worldName)));
			}
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
	
	/**
	 * Renames or deletes a group. If the newName is null, it will delete the group,
	 * otherwise it will rename it.
	 * @param group The group to rename or delete.
	 * @param newName The new name, or null to delete.
	 */
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
	
	/**
	 * Promotes a User along a promotion ladder, based on their primary group.
	 * @param user The User to promote.
	 * @param world The world name to promote in. Leave null to promote on the server level.
	 * @param sender The command sender that is issuing the promotion (used for checking promote permissions).
	 * @return Whether the promotion was successful.
	 */
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

	/**
	 * Demotes a User along a promotion ladder, based on their primary group.
	 * @param user The User to demote.
	 * @param world The world name to demote in. Leave null to demote on the server level.
	 * @param sender The command sender that is issuing the demotion (used for checking demote permissions).
	 * @return Whether the demotion was successful.
	 */
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
		
	protected void setPlayerListName(Player player, User user) {
		if (updatePlayerList) {
			String world = player.getWorld().getName();
			String name = user.getColor(world) + player.getName();
			if (name.length() > 15) {
				name = name.substring(0, 15);
			}
			player.setPlayerListName(name);
		}		
	}
	
	/**
	 * Removes and saves the player's User object, and removes their permission attachment.
	 * @param player The player to unload.
	 */
	public void unloadPlayer(Player player) {
		String playerName = player.getName().toLowerCase();
		players.remove(playerName).save();
		attachments.remove(playerName).remove();
	}
	
	/**
	 * Removes and saves the player's User object, and removes their permission attachment.
	 * @param The player name to unload.
	 */
	public void unloadPlayer(String playerName) {
		playerName = playerName.toLowerCase();
		players.remove(playerName).save();
		attachments.remove(playerName).remove();
	}
	
	/**
	 * Saves all user and group data.
	 */
	public void saveAll() {
		for (User user : players.values()) {
			user.save();
		}
		for (Group group : groups.values()) {
			group.save();
		}
	}
	
	/**
	 * Creates a new group with the specified name. This does NOT check if the group already exists,
	 * the getGroup() method should be used first to check for existance.
	 * @param name The name for the new group.
	 * @return The new group.
	 */
	public static Group newGroup(String name) {
		Group group = new Group(name);
		plugin.groups.put(name.toLowerCase(), group);
		return group;
	}
	
	/**
	 * Gets a group by the specified name, if one exists.
	 * @param name The group's name.
	 * @return The group, or null if no group with that name exists.
	 */
	public static Group getGroup(String name) {
		return plugin.groups.get(name.toLowerCase());
	}
	
	/**
	 * Gets the default group that players join if they have no group.
	 * @return The default group.
	 */
	public static Group getDefaultGroup() {
		return plugin.defaultGroup;
	}
	
	/**
	 * Gets all valid group names.
	 * @return All group names.
	 */
	public static Set<String> getGroupNames() {
		return plugin.groups.keySet();
	}
	
	/**
	 * Gets the YAPP plugin object.
	 * @return The plugin object.
	 */
	public static YAPP getPlugin() {
		return plugin;
	}
	
	protected static StorageMethod getStorage() {
		return plugin.storageMethod;
	}
	
	public static void log(String message) {
		plugin.getLogger().info(message);
	}
	
	public static void warning(String message) {
		plugin.getLogger().warning(message);
	}
	
	public static void error(String message) {
		plugin.getLogger().severe(message);
	}
	
	public static void debug(String message) {
		if (debug) {
			plugin.getLogger().info(message);
		}
	}
	
}
