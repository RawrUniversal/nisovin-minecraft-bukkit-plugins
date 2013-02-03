package com.nisovin.yapp;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

class CommandConvert implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender.isOp() && sender instanceof ConsoleCommandSender) {
			if (args.length == 0) {
				sender.sendMessage("You must specify a plugin to convert from (pex or permbukkit)");
			} else if (args[0].equalsIgnoreCase("pex") || args[0].equalsIgnoreCase("permissionsex")) {
				convertFromPex();
			} else if (args[0].equalsIgnoreCase("permbukkit") || args[0].equalsIgnoreCase("permissionsbukkit")) {
				convertFromPermissionsBukkit();
			} else if (args[0].equalsIgnoreCase("gm") || args[0].equalsIgnoreCase("groupmanager")) {
				convertFromGroupManager();
			}
		}
		
		return true;
	}

	private void convertFromPex() {
		File file = new File("plugins/PermissionsEx/permissions.yml");
		if (!file.exists()) return;
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// get groups
		ConfigurationSection groupsSection = config.getConfigurationSection("groups");
		HashMap<Group, HashMap<String, List<String>>> allInheritedGroups = new HashMap<Group, HashMap<String, List<String>>>(); // gotta handle this later
		if (groupsSection != null) {
			Set<String> groupKeys = groupsSection.getKeys(false);
			for (String groupName : groupKeys) {
				// get group
				ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
				Group group = YAPP.getGroup(groupName);
				if (group == null) group = YAPP.newGroup(groupName);
				
				// prepare inherited group storage
				HashMap<String, List<String>> inheritedGroups = new HashMap<String, List<String>>();
				allInheritedGroups.put(group, inheritedGroups);
				
				// get group's inherited groups
				List<String> inheritedGroupNames = groupSection.getStringList("inheritance");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					inheritedGroups.put(null, inheritedGroupNames);
				}
				
				// get group's permissions and add to group
				List<String> permissions = groupSection.getStringList("permissions");
				if (permissions != null) {
					for (String permission : permissions) {
						group.addPermission(permission);
					}
				}
				
				// get worlds
				if (groupSection.contains("worlds")) {
					ConfigurationSection worldsSection = groupSection.getConfigurationSection("worlds");
					Set<String> worldNames = worldsSection.getKeys(false);
					for (String worldName : worldNames) {
						ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
						
						// get world inherited groups
						List<String> worldInheritedGroupNames = worldSection.getStringList("inheritance");
						if (worldInheritedGroupNames != null && worldInheritedGroupNames.size() > 0) {
							inheritedGroups.put(worldName, worldInheritedGroupNames);
						}
						
						// get world permissions
						List<String> worldPermissions = worldSection.getStringList("permissions");
						if (worldPermissions != null) {
							for (String permission : worldPermissions) {
								group.addPermission(worldName, permission);
							}
						}
						
						// get world data
						if (worldSection.contains("prefix")) {
							group.setPrefix(worldName, worldSection.getString("prefix"));
						}
					}
				}
				
				// get data
				if (groupSection.contains("prefix")) {
					group.setPrefix(null, groupSection.getString("prefix"));
				}
			}
		}
		
		// add group inherited groups
		for (Group group : allInheritedGroups.keySet()) {
			HashMap<String, List<String>> inheritedGroupsByWorld = allInheritedGroups.get(group);
			for (String world : inheritedGroupsByWorld.keySet()) {
				List<String> inheritedGroups = inheritedGroupsByWorld.get(world);
				for (String groupName : inheritedGroups) {
					Group g = YAPP.getGroup(groupName);
					if (g != null) {
						group.addGroup(world, g);
					}
				}
			}
		}
		
		// get users
		ConfigurationSection usersSection = config.getConfigurationSection("users");
		if (usersSection != null) {
			Set<String> userKeys = usersSection.getKeys(false);
			for (String userName : userKeys) {
				// get user
				ConfigurationSection userSection = usersSection.getConfigurationSection(userName);
				User user = YAPP.getPlayerUser(userName);
				if (YAPP.getDefaultGroup() != null && user.getGroups(null).size() == 1 && user.getGroups(null).contains(YAPP.getDefaultGroup())) {
					user.removeGroup(null, YAPP.getDefaultGroup());
				}
				
				// get group's inherited groups				
				List<String> inheritedGroupNames = userSection.getStringList("group");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					for (String groupName : inheritedGroupNames) {
						Group group = YAPP.getGroup(groupName);
						if (group != null) {
							user.addGroup(group);
						}
					}
				}
				
				// get group's permissions and add to group
				List<String> permissions = userSection.getStringList("permissions");
				if (permissions != null) {
					for (String permission : permissions) {
						user.addPermission(permission);
					}
				}
				
				// get worlds
				if (userSection.contains("worlds")) {
					ConfigurationSection worldsSection = userSection.getConfigurationSection("worlds");
					Set<String> worldNames = worldsSection.getKeys(false);
					for (String worldName : worldNames) {
						ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
						
						// get world inherited groups
						List<String> worldInheritedGroupNames = worldSection.getStringList("group");
						if (worldInheritedGroupNames != null && worldInheritedGroupNames.size() > 0) {
							for (String groupName : worldInheritedGroupNames) {
								Group group = YAPP.getGroup(groupName);
								if (group != null) {
									user.addGroup(worldName, group);
								}
							}
						}
						
						// get world permissions
						List<String> worldPermissions = worldSection.getStringList("permissions");
						if (worldPermissions != null) {
							for (String permission : worldPermissions) {
								user.addPermission(worldName, permission);
							}
						}
					}
				}
				
				// get data
				if (userSection.contains("prefix")) {
					user.setPrefix(null, userSection.getString("prefix"));
				}
			}
		}
		
		YAPP.plugin.saveAll();
	}

	private void convertFromPermissionsBukkit() {
		File file = new File("plugins/PermissionsBukkit/config.yml");
		if (!file.exists()) return;
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// get groups
		ConfigurationSection groupsSection = config.getConfigurationSection("groups");
		HashMap<Group, HashMap<String, List<String>>> allInheritedGroups = new HashMap<Group, HashMap<String, List<String>>>(); // gotta handle this later
		if (groupsSection != null) {
			Set<String> groupKeys = groupsSection.getKeys(false);
			for (String groupName : groupKeys) {
				// get group
				ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
				Group group = YAPP.getGroup(groupName);
				if (group == null) group = YAPP.newGroup(groupName);
				
				// prepare inherited group storage
				HashMap<String, List<String>> inheritedGroups = new HashMap<String, List<String>>();
				allInheritedGroups.put(group, inheritedGroups);
				
				// get group's inherited groups
				List<String> inheritedGroupNames = groupSection.getStringList("inheritance");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					inheritedGroups.put(null, inheritedGroupNames);
				}
				
				// get group's permissions and add to group
				ConfigurationSection permissionsSection = groupSection.getConfigurationSection("permissions");
				if (permissionsSection != null) {
					Set<String> permissionKeys = permissionsSection.getKeys(true);
					for (String permission : permissionKeys) {
						if (permissionsSection.isBoolean(permission)) {
							boolean value = permissionsSection.getBoolean(permission, true);
							group.addPermission((value ? "" : "-") + permission);
						}
					}
				}
				
				// get worlds
				if (groupSection.contains("worlds")) {
					ConfigurationSection worldsSection = groupSection.getConfigurationSection("worlds");
					Set<String> worldNames = worldsSection.getKeys(false);
					for (String worldName : worldNames) {
						ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);						
						
						// get world permissions						
						Set<String> worldPermissions = worldSection.getKeys(true);
						if (worldPermissions != null) {
							for (String permission : worldPermissions) {
								if (worldSection.isBoolean(permission)) {
									boolean value = worldSection.getBoolean(permission, true);
									group.addPermission(worldName, (value ? "" : "-") + permission);
								}
							}
						}
					}
				}
			}
		}
		
		// add group inherited groups
		for (Group group : allInheritedGroups.keySet()) {
			HashMap<String, List<String>> inheritedGroupsByWorld = allInheritedGroups.get(group);
			for (String world : inheritedGroupsByWorld.keySet()) {
				List<String> inheritedGroups = inheritedGroupsByWorld.get(world);
				for (String groupName : inheritedGroups) {
					Group g = YAPP.getGroup(groupName);
					if (g != null) {
						group.addGroup(world, g);
					}
				}
			}
		}
		
		// get users
		ConfigurationSection usersSection = config.getConfigurationSection("users");
		if (usersSection != null) {
			Set<String> userKeys = usersSection.getKeys(false);
			for (String userName : userKeys) {
				// get user
				ConfigurationSection userSection = usersSection.getConfigurationSection(userName);
				User user = YAPP.getPlayerUser(userName);
				if (YAPP.getDefaultGroup() != null && user.getGroups(null).size() == 1 && user.getGroups(null).contains(YAPP.getDefaultGroup())) {
					user.removeGroup(null, YAPP.getDefaultGroup());
				}
				
				// get group's inherited groups				
				List<String> inheritedGroupNames = userSection.getStringList("groups");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					for (String groupName : inheritedGroupNames) {
						Group group = YAPP.getGroup(groupName);
						if (group != null) {
							user.addGroup(group);
						}
					}
				}
				
				// get user's permissions and add to group
				ConfigurationSection permissionsSection = userSection.getConfigurationSection("permissions");
				if (permissionsSection != null) {
					Set<String> permissionKeys = permissionsSection.getKeys(true);
					for (String permission : permissionKeys) {
						if (permissionsSection.isBoolean(permission)) {
							boolean value = permissionsSection.getBoolean(permission, true);
							user.addPermission((value ? "" : "-") + permission);
						}
					}
				}
				
				// get worlds
				if (userSection.contains("worlds")) {
					ConfigurationSection worldsSection = userSection.getConfigurationSection("worlds");
					Set<String> worldNames = worldsSection.getKeys(false);
					for (String worldName : worldNames) {
						ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
												
						// get world permissions					
						Set<String> worldPermissions = worldSection.getKeys(true);
						if (worldPermissions != null) {
							for (String permission : worldPermissions) {
								if (worldSection.isBoolean(permission)) {
									boolean value = worldSection.getBoolean(permission, true);
									user.addPermission(worldName, (value ? "" : "-") + permission);
								}
							}
						}
					}
				}
			}
		}
		
		YAPP.plugin.saveAll();
	}

	private void convertFromGroupManager() {
		File folder = new File("plugins/GroupManager");
		if (!folder.exists() || !folder.isDirectory()) return;		
		
		// get global groups
		File globalGroupsFile = new File(folder, "globalgroups.yml");
		if (globalGroupsFile.exists()) {
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(globalGroupsFile);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			
			// process data
			ConfigurationSection groupsSection = config.getConfigurationSection("groups");
			if (groupsSection != null) {
				Set<String> groupKeys = groupsSection.getKeys(false);
				for (String groupName : groupKeys) {
					// get group
					ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
					Group group = YAPP.getGroup("global_" + groupName);
					if (group == null) group = YAPP.newGroup("global_" + groupName);
					
					// get permissions
					List<String> permissions = groupSection.getStringList("permissions");
					if (permissions != null) {
						for (String permission : permissions) {
							group.addPermission(permission);
						}
					}
				}
			}
		}
		
		// get world groups
		File[] worldFolders = folder.listFiles();
		HashMap<Group, HashMap<String, List<String>>> allInheritedGroups = new HashMap<Group, HashMap<String, List<String>>>(); // gotta handle this later
		for (File worldFolder : worldFolders) {
			if (!worldFolder.isDirectory()) continue;
			
			String worldName = worldFolder.getName();
			
			File groupsFile = new File(worldFolder, "groups.yml");
			if (groupsFile.exists()) {
				// load file
				YamlConfiguration config = new YamlConfiguration();
				try {
					config.load(groupsFile);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
				// process data
				ConfigurationSection groupsSection = config.getConfigurationSection("groups");
				if (groupsSection != null) {
					Set<String> groupKeys = groupsSection.getKeys(false);
					for (String groupName : groupKeys) {
						// get group
						ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
						Group group = YAPP.getGroup(groupName);
						if (group == null) group = YAPP.newGroup(groupName);
						
						// prepare inherited group storage
						HashMap<String, List<String>> inheritedGroups = new HashMap<String, List<String>>();
						allInheritedGroups.put(group, inheritedGroups);
						
						// get group's inherited groups
						List<String> inheritedGroupNames = groupSection.getStringList("inheritance");
						if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
							inheritedGroups.put(worldName, inheritedGroupNames);
						}
						
						// get group's permissions and add to group
						List<String> permissions = groupSection.getStringList("permissions");
						if (permissions != null) {
							for (String permission : permissions) {
								group.addPermission(worldName, permission);
							}
						}
						
						// get info
						if (groupSection.contains("info.prefix")) {
							group.setPrefix(worldName, groupSection.getString("info.prefix"));
						}
					}
				}				
				
			}
		}
		
		// add group inherited groups
		for (Group group : allInheritedGroups.keySet()) {
			HashMap<String, List<String>> inheritedGroupsByWorld = allInheritedGroups.get(group);
			for (String world : inheritedGroupsByWorld.keySet()) {
				List<String> inheritedGroups = inheritedGroupsByWorld.get(world);
				for (String groupName : inheritedGroups) {
					Group g = YAPP.getGroup(groupName.startsWith("g:") ? groupName.replace("g:", "global_") : groupName);
					if (g != null) {
						group.addGroup(world, g);
					}
				}
			}
		}
		
		// get users
		for (File worldFolder : worldFolders) {
			if (!worldFolder.isDirectory()) continue;
			
			String worldName = worldFolder.getName();
			
			File usersFile = new File(worldFolder, "users.yml");
			if (usersFile.exists()) {
				// load file
				YamlConfiguration config = new YamlConfiguration();
				try {
					config.load(usersFile);
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				
				// process data
				ConfigurationSection usersSection = config.getConfigurationSection("users");
				if (usersSection != null) {
					Set<String> userKeys = usersSection.getKeys(false);
					for (String userName : userKeys) {
						// get user
						ConfigurationSection userSection = usersSection.getConfigurationSection(userName);
						User user = YAPP.getPlayerUser(userName);
						if (YAPP.getDefaultGroup() != null && user.getGroups(null).size() == 1 && user.getGroups(null).contains(YAPP.getDefaultGroup())) {
							user.removeGroup(null, YAPP.getDefaultGroup());
						}
						
						// get user's inherited groups
						if (userSection.contains("group")) {
							String groupName = userSection.getString("group");
							Group group = YAPP.getGroup(groupName.startsWith("g:") ? groupName.replace("g:", "global_") : groupName);
							if (group != null) {
								user.addGroup(worldName, group);
							}
						}
						List<String> inheritedGroupNames = userSection.getStringList("subgroups");
						if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
							for (String groupName : inheritedGroupNames) {
								Group group = YAPP.getGroup(groupName.startsWith("g:") ? groupName.replace("g:", "global_") : groupName);
								if (group != null) {
									user.addGroup(worldName, group);
								}
							}
						}
						
						// get user's permissions and add to user
						List<String> permissions = userSection.getStringList("permissions");
						if (permissions != null) {
							for (String permission : permissions) {
								user.addPermission(worldName, permission);
							}
						}
						
						// get info
						if (userSection.contains("info.prefix")) {
							user.setPrefix(worldName, userSection.getString("info.prefix"));
						}
					}
				}
			}
		}
		
		YAPP.plugin.saveAll();
	}

}
