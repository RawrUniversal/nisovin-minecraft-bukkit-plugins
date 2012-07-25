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

public class CommandConvert implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (sender.isOp() && sender instanceof ConsoleCommandSender) {
			if (args.length == 0) {
				sender.sendMessage("You must specify a plugin to convert from (pex or permbukkit)");
			} else if (args[0].equalsIgnoreCase("pex")) {
				convertFromPex();
			} else if (args[0].equalsIgnoreCase("permbukkit")) {
				convertFromPermissionsBukkit();
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
				Group group = MainPlugin.getGroup(groupName);
				if (group == null) group = MainPlugin.newGroup(groupName);
				
				// prepare inherited group storage
				HashMap<String, List<String>> inheritedGroups = new HashMap<String, List<String>>();
				allInheritedGroups.put(group, inheritedGroups);
				
				// get group's inherited groups
				List<String> inheritedGroupNames = groupSection.getStringList("inheritence");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					inheritedGroups.put(null, inheritedGroupNames);
				}
				inheritedGroupNames = groupSection.getStringList("inheritance");
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
						List<String> worldInheritedGroupNames = worldSection.getStringList("group");
						if (worldInheritedGroupNames != null && worldInheritedGroupNames.size() > 0) {
							inheritedGroups.put(worldName, worldInheritedGroupNames);
							System.out.println("HELLO! " + groupName + " " + worldName + " " + worldInheritedGroupNames.size());
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
							System.out.println("FOUND WORLD PREFIX! " + groupName + " " + worldName + " " + worldSection.getString("prefix"));
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
					Group g = MainPlugin.getGroup(groupName);
					if (g != null) {
						group.addGroup(world, g);
						System.out.println("HELLO AGAIN! " + group.getName() + " " + world + " " + groupName);
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
				User user = MainPlugin.getPlayerUser(userName);
				if (MainPlugin.getDefaultGroup() != null && user.getGroups(null).size() == 1 && user.getGroups(null).contains(MainPlugin.getDefaultGroup())) {
					user.removeGroup(null, MainPlugin.getDefaultGroup());
				}
				
				// get group's inherited groups				
				List<String> inheritedGroupNames = userSection.getStringList("group");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					for (String groupName : inheritedGroupNames) {
						Group group = MainPlugin.getGroup(groupName);
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
								Group group = MainPlugin.getGroup(groupName);
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
		
		MainPlugin.yapp.saveAll();
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
				Group group = MainPlugin.getGroup(groupName);
				if (group == null) group = MainPlugin.newGroup(groupName);
				
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
					Group g = MainPlugin.getGroup(groupName);
					if (g != null) {
						group.addGroup(world, g);
						System.out.println("HELLO AGAIN! " + group.getName() + " " + world + " " + groupName);
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
				User user = MainPlugin.getPlayerUser(userName);
				if (MainPlugin.getDefaultGroup() != null && user.getGroups(null).size() == 1 && user.getGroups(null).contains(MainPlugin.getDefaultGroup())) {
					user.removeGroup(null, MainPlugin.getDefaultGroup());
				}
				
				// get group's inherited groups				
				List<String> inheritedGroupNames = userSection.getStringList("groups");
				if (inheritedGroupNames != null && inheritedGroupNames.size() > 0) {
					for (String groupName : inheritedGroupNames) {
						Group group = MainPlugin.getGroup(groupName);
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
		
		MainPlugin.yapp.saveAll();
	}


}
