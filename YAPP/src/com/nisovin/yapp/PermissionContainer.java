package com.nisovin.yapp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.World;

public class PermissionContainer implements Comparable<PermissionContainer> {

	private String name;
	private String type;
	
	private List<Group> groups = new ArrayList<Group>();
	private List<PermissionNode> permissions = new ArrayList<PermissionNode>();
	private Map<String,List<Group>> worldGroups = new HashMap<String,List<Group>>();
	private Map<String,List<PermissionNode>> worldPermissions = new HashMap<String,List<PermissionNode>>();	
	private Map<String,List<PermissionNode>> cached = new HashMap<String,List<PermissionNode>>();
	
	private Map<String,String> info = new LinkedHashMap<String,String>();
	private String description = "";
	private ChatColor color = null;
	private String prefix = null;
	private ChatColor cachedColor = null;
	private String cachedPrefix = null;
	
	private boolean dirty = false;
	
	public PermissionContainer(String name, String type) {
		this.name = name;
		this.type = type;
		this.dirty = true;
	}
	
	public PermissionContainer(PermissionContainer other, String name) {
		this.name = name;
		this.type = other.type;
		
		this.groups = new ArrayList<Group>(other.groups);
		this.permissions = new ArrayList<PermissionNode>(other.permissions);
		this.worldGroups = new HashMap<String,List<Group>>();
		for (String s : other.worldGroups.keySet()) {
			this.worldGroups.put(s, new ArrayList<Group>(other.worldGroups.get(s)));
		}
		this.worldPermissions = new HashMap<String,List<PermissionNode>>();
		for (String s : other.worldPermissions.keySet()) {
			this.worldPermissions.put(s, new ArrayList<PermissionNode>(other.worldPermissions.get(s)));
		}
		
		this.info = new LinkedHashMap<String,String>(other.info);
		this.description = other.description;
		this.color = other.color;
		this.prefix = other.prefix;
		
		this.dirty = true;
	}
	
	public List<PermissionNode> getAllPermissions(World world) {
		return getAllPermissions(world.getName());
	}
	
	public List<PermissionNode> getAllPermissions(String world) {
		if (world == null) world = "";
		if (cached.containsKey(world)) {
			return cached.get(world);
		} else {
			List<PermissionNode> nodes = new ArrayList<PermissionNode>();
			
			// add world perms
			if (!world.isEmpty()) {
				if (worldPermissions.containsKey(world)) {
					for (PermissionNode node : worldPermissions.get(world)) {
						if (!nodes.contains(node)) {
							nodes.add(node);
						}
					}
				}
			}
			
			// add own perms
			for (PermissionNode node : permissions) {
				if (!nodes.contains(node)) {
					nodes.add(node);
				}
			}
			
			// add world group perms
			if (!world.isEmpty()) {
				if (worldGroups.containsKey(world)) {
					for (Group group : worldGroups.get(world)) {
						List<PermissionNode> groupNodes = group.getAllPermissions(world);
						for (PermissionNode node : groupNodes) {
							if (!nodes.contains(node)) {
								nodes.add(node);
							}
						}
					}
				}
			}
			
			// add group perms
			for (Group group : groups) {
				List<PermissionNode> groupNodes = group.getAllPermissions(world);
				for (PermissionNode node : groupNodes) {
					if (!nodes.contains(node)) {
						nodes.add(node);
					}
				}
			}
			
			cached.put(world, nodes);
			return nodes;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String desc) {
		this.description = desc;
		info.put("description", desc);
		dirty = true;
	}
	
	public ChatColor getColor() {
		return getColor(null);
	}
	
	public ChatColor getColor(String world) {
		if (cachedColor != null) {
			return cachedColor;
		} else if (color != null) {
			cachedColor = color;
			return cachedColor;
		} else {
			Group group = getPrimaryGroup(world);
			if (group != null) {
				cachedColor = group.getColor(world);
				if (cachedColor != null) {
					return cachedColor;
				}
			}
		}
		cachedColor = ChatColor.WHITE;
		return cachedColor;
	}
	
	public void setColor(String color) {
		if (color == null || color.length() == 0) {
			setColor((ChatColor)null);
		} else if (color.length() == 1) {
			setColor(ChatColor.getByChar(color));
		} else {
			try {
				setColor(ChatColor.valueOf(color.replace(" ", "_").toUpperCase()));
			} catch (IllegalArgumentException e) {
				setColor((ChatColor)null);
			}
		}
	}
	
	public void setColor(ChatColor color) {
		this.color = color;
		if (color != null) {
			info.put("color", color.name().replace("_", " ").toLowerCase());
		} else {
			info.remove("color");
		}
		cachedColor = null;
		dirty = true;
	}
	
	public String getPrefix() {
		return getPrefix(null);
	}
	
	public String getPrefix(String world) {
		if (cachedPrefix != null) {
			return cachedPrefix;
		} else if (prefix != null) {
			cachedPrefix = colorify(prefix);
			return cachedPrefix;
		} else {
			Group group = getPrimaryGroup(world);
			if (group != null) {
				cachedPrefix = group.getPrefix(world);
				if (cachedPrefix != null) {
					return cachedPrefix;
				}
			}
		}
		cachedPrefix = "";
		return cachedPrefix;
	}
	
	public void setPrefix(String prefix) {
		if (prefix != null && !prefix.isEmpty()) {
			prefix = prefix.replace("\u00A7$1", "&");
			this.prefix = prefix;
			info.put("prefix", prefix);
		} else {
			this.prefix = null;
			info.remove("prefix");
		}
		cachedPrefix = null;
		dirty = true;
	}
	
	public String getInfo(String key) {
		return info.get(key.toLowerCase());
	}
	
	public void setInfo(String key, String value) {
		key = key.toLowerCase();
		if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
			value = value.substring(1, value.length() - 1);
		}
		if (key.equals("color")) {
			setColor(value);
		} else if (key.equals("prefix")) {
			setPrefix(value);
		} else if (key.equals("description")) {
			setDescription(value);
		} else {
			if (value != null && !value.isEmpty()) {
				info.put(key, value);
			} else {
				info.remove(key);
			}
			dirty = true;
		}
	}
	
	private String colorify(String s) {
		return s.replaceAll("&([0-9a-fk])", "\u00A7$1");
	}
	
	public List<PermissionNode> getActualPermissionList() {
		return permissions;
	}
	
	public List<Group> getActualGroupList() {
		return groups;
	}
	
	public String getActualPrefix() {
		if (prefix == null) {
			return null;
		} else {
			return colorify(prefix);
		}
	}
	
	public ChatColor getActualColor() {
		return color;
	}
	
	public boolean has(String world, String permission) {
		List<PermissionNode> nodes = getAllPermissions(world);
		for (PermissionNode node : nodes) {
			if (node.getNodeName().equals(permission) && node.getValue() == true) {
				return true;
			}
		}
		return false;
	}
	
	public boolean inGroup(Group group, boolean recurse) {
		return inGroup(null, group, recurse);
	}
	
	public boolean inGroup(String world, Group group, boolean recurse) {
		if (groups.contains(group)) {
			return true;
		} else if (world != null && !world.isEmpty()) {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups != null) {
				if (wgroups.contains(group)) {
					return true;
				}
			}
		}
		if (recurse) {
			for (Group g : groups) {
				if (g.inGroup(world, group, true)) {
					return true;
				}
			}
			if (world != null && !world.isEmpty()) {
				List<Group> wgroups = worldGroups.get(world);
				if (wgroups != null) {
					for (Group g : wgroups) {
						if (g.inGroup(world, group, true)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public Set<Group> getGroups(String world) {
		Set<Group> g = new HashSet<Group>();
		g.addAll(groups);
		if (world != null && !world.isEmpty()) {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups != null) {
				g.addAll(wgroups);
			}
		}
		return g;
	}
	
	public Group getPrimaryGroup(String world) {
		if (world != null && !world.isEmpty()) {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups != null && wgroups.size() > 0) {
				return wgroups.get(0);
			} else if (groups.size() > 0) {
				return groups.get(0);
			}
		} else if (groups.size() > 0) {
			return groups.get(0);
		}
		return null;
	}
	
	public boolean setPrimaryGroup(Group group) {
		return setPrimaryGroup(null, group);
	}
	
	public boolean setPrimaryGroup(String world, Group group) {
		if (world != null && !world.isEmpty()) {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups != null && wgroups.size() > 0) {
				if (wgroups.contains(group)) {
					wgroups.remove(group);
					wgroups.add(0, group);
					dirty = true;
					return true;
				}
			}
		} else if (groups.contains(group)) {
			groups.remove(group);
			groups.add(0, group);
			dirty = true;
			return true;
		}
		return false;
	}
	
	public boolean addPermission(String permission) {
		return addPermission(null, permission);
	}
	
	public boolean addPermission(String world, String permission) {
		PermissionNode node = new PermissionNode(permission);
		if (world == null || world.isEmpty()) {
			// add to base perms
			permissions.remove(node);
			permissions.add(node);
			dirty = true;
		} else {
			List<PermissionNode> nodes = worldPermissions.get(world);
			if (nodes == null) {
				nodes = new ArrayList<PermissionNode>();
				worldPermissions.put(world, nodes);
			}
			nodes.remove(node);
			nodes.add(node);
			dirty = true;
		}
		return true;
	}
	
	public boolean removePermission(String world, String permission) {
		if (world == null || world.isEmpty()) {
			boolean ok = permissions.remove(new PermissionNode(permission));
			if (ok) {
				dirty = true;
			}
			return ok;
		} else {
			List<PermissionNode> nodes = worldPermissions.get(world);
			if (nodes == null) {
				return false;
			} else {
				boolean ok = nodes.remove(new PermissionNode(permission));
				if (ok) {
					dirty = true;
				}
				return ok;
			}
		}		
	}
	
	public boolean addGroup(Group group) {
		return addGroup(null, group);
	}
	
	public boolean addGroup(String world, Group group) {
		// TODO: check for infinite recursion
		if (world == null || world.isEmpty()) {
			groups.add(group);
			dirty = true;
		} else {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups == null) {
				wgroups = new ArrayList<Group>();
				worldGroups.put(world, wgroups);
			}
			wgroups.add(group);
			dirty = true;
		}
		return true;
	}
	
	public boolean removeGroup(String world, Group group) {
		if (world == null || world.isEmpty()) {
			boolean ok = groups.remove(group);
			if (ok) {
				dirty = true;
			}
			return ok;
		} else {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups == null) {
				return false;
			} else {
				boolean ok = wgroups.remove(group);
				if (ok) {
					dirty = true;
				}
				return ok;
			}
		}
	}
	
	public void replaceGroup(Group oldGroup, Group newGroup) {
		int index = groups.indexOf(oldGroup);
		if (index >= 0) {
			if (newGroup != null) {
				groups.set(index, newGroup);
			} else {
				groups.remove(index);
			}
			dirty = true;
		}
		for (List<Group> wgroups : worldGroups.values()) {
			index = wgroups.indexOf(oldGroup);
			if (index >= 0) {
				if (newGroup != null) {
					wgroups.set(index, newGroup);
				} else {
					wgroups.remove(index);
				}
				dirty = true;
			}
		}
	}
	
	public void clearCache() {
		cached.clear();
		resetCachedInfo();
	}
	
	public void resetCachedInfo() {
		cachedColor = null;
		cachedPrefix = null;
	}
	
	public void loadFromFiles() {
		MainPlugin.debug("Loading " + type + " '" + name + "'");
		File dataFolder = MainPlugin.yapp.getDataFolder();
		
		// get main file
		MainPlugin.debug("  Loading base data");
		File file = new File(dataFolder, type + "s" + File.separator + name + ".txt");
		if (file.exists()) {
			loadFromFile(file, groups, permissions);
		}
		
		// get world files
		file = new File(dataFolder, "worlds");
		if (file.exists()) {
			File[] files = file.listFiles();
			// find folders in plugin folder
			for (File f : files) {
				if (f.isDirectory()) {
					String worldName = f.getName();
					// get folder in world folder
					File groupsFolder = new File(f, type + "s");
					if (groupsFolder.exists() && groupsFolder.isDirectory()) {
						// get all files in folder
						File[] groupFiles = groupsFolder.listFiles();
						for (File groupFile : groupFiles) {
							if (groupFile.getName().equals(name + ".txt")) {
								// load file
								MainPlugin.debug("  Loading world data '" + worldName + "'");
								List<PermissionNode> perms = new ArrayList<PermissionNode>();
								worldPermissions.put(worldName, perms);
								List<Group> wgroups = new ArrayList<Group>();
								worldGroups.put(worldName, wgroups);
								loadFromFile(groupFile, wgroups, perms);
							}
						}
					}
				}
			}
		}
		
		dirty = false;
	}
	
	public void loadFromFile(File file, List<Group> groups, List<PermissionNode> perms) {
		try {
			String mode = "";
			Scanner scanner = new Scanner(file);
			String line;
			while (scanner.hasNext()) {
				line = scanner.nextLine().trim();
				if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
					// ignore
				} else if (line.startsWith("=")) {
					// mode change
					line = line.replace("=", "").trim().toLowerCase();
					if (line.startsWith("data") || line.startsWith("info")) {
						mode = "info";
						MainPlugin.debug("    Reading info");
					} else if (line.startsWith("inherit") || line.startsWith("group")) {
						mode = "groups";
						MainPlugin.debug("    Reading groups");
					} else if (line.startsWith("perm")) {
						mode = "perms";
						MainPlugin.debug("    Reading perms");
					}
				} else if (mode.equals("info")) {
					String key = null, val = null;
					if (line.contains("=")) {
						String[] s = line.split("=", 2);
						key = s[0].trim();
						val = s[1].trim();
					} else if (line.contains(":")) {
						String[] s = line.split(":", 2);
						key = s[0].trim();
						val = s[1].trim();
					}
					if (key != null && val != null) {
						key = key.toLowerCase();
						if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
							val = val.substring(1, val.length() - 1);
						}
						info.put(key, val);
						if (key.equals("description")) {
							description = val;
						} else if (key.equals("color")) {
							if (val.length() == 1) {
								color = ChatColor.getByChar(val);
							} else {
								color = ChatColor.valueOf(val.replace(" ", "_").toUpperCase());
							}
						} else if (key.equals("prefix")) {
							prefix = val;
						}
						MainPlugin.debug("      Added info: " + key + " = " + val);
					} else {
						MainPlugin.warning(type + " '" + name + "' has invalid info line: " + line);
					}
				} else if (mode.equals("groups")) {
					// inherited group
					Group group = MainPlugin.getGroup(line);
					if (group != null) {
						groups.add(group);
						MainPlugin.debug("      Added inherited group: " + line);
					} else {
						MainPlugin.warning(type + " '" + name + "' has non-existant inherited group '" + line + "'");
					}
				} else if (mode.equals("perms")) {
					// permission
					PermissionNode node = new PermissionNode(line);
					perms.add(node);
					MainPlugin.debug("      Added permission: " + node);
				} else {
					MainPlugin.warning(type + " '" + name + "' has orphan line: " + line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	public void save() {
		save(false);
	}
	
	public void save(boolean force) {
		if (dirty || force) {
			dirty = false;
			
			BufferedWriter file = null;
			
			// save base data
			try {
				file = writer(null);
				// save info
				if (info.size() > 0) {
					writeInfo(file, info);
				}
				// save groups
				if (groups.size() > 0) {
					writeGroups(file, groups);
				}
				// save perms
				if (permissions.size() > 0) {
					writePermissions(file, permissions);
				}
				file.close();
			} catch (IOException e) {
				MainPlugin.error("Failed to write file for " + type + " '" + name + "'!");
			}
			
			// save world data
			Set<String> worldNames = new HashSet<String>();
			worldNames.addAll(worldPermissions.keySet());
			worldNames.addAll(worldGroups.keySet());
			List<Group> wgroups = null;
			List<PermissionNode> wperms = null;
			for (String worldName : worldNames) {
				try {
					file = writer(worldName);
					wgroups = worldGroups.get(worldName);
					wperms = worldPermissions.get(worldName);
					// save groups
					if (wgroups != null && wgroups.size() > 0) {
						writeGroups(file, wgroups);
					}
					// save perms
					if (wperms != null && wperms.size() > 0) {
						writePermissions(file, wperms);
					}
					file.close();
				} catch (IOException e) {
					MainPlugin.error("Failed to write file for " + type + " '" + name + "' for world '" + worldName + "'!");
				}
			}
		}
	}
	
	private void writeInfo(BufferedWriter file, Map<String, String> info) throws IOException {
		file.write("== INFORMATION ==");
		file.newLine();
		file.newLine();
		for (String key : info.keySet()) {
			file.write(key + " : \"" + info.get(key) + "\"");
			file.newLine();
		}
		file.newLine();
	}
	
	private void writeGroups(BufferedWriter file, List<Group> groups) throws IOException {
		file.write("== GROUPS ==");
		file.newLine();
		file.newLine();
		for (Group g : groups) {
			file.write(g.getName());
			file.newLine();
		}
		file.newLine();
	}
	
	private void writePermissions(BufferedWriter file, List<PermissionNode> perms) throws IOException {
		file.write("== PERMISSIONS ==");
		file.newLine();
		file.newLine();
		for (PermissionNode n : perms) {
			file.write((n.getValue() == true ? " + " : " - ") + n.getNodeName());
			file.newLine();
		}
		file.newLine();
	}
	
	private BufferedWriter writer(String worldName) throws IOException {
		File file;
		if (worldName == null) {
			file = new File(MainPlugin.yapp.getDataFolder(), type + "s" + File.separator + name + ".txt");
		} else {
			file = new File(MainPlugin.yapp.getDataFolder(), "worlds" + File.separator + worldName + File.separator + type + "s" + File.separator + name + ".txt");
		}
		file.mkdirs();
		if (file.exists()) file.delete();
		
		return new BufferedWriter(new FileWriter(file));
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof PermissionContainer && ((PermissionContainer)o).name.equals(name));
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(PermissionContainer o) {
		return name.compareTo(o.name);
	}
}
