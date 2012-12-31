package com.nisovin.yapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;

public class PermissionContainer implements Comparable<PermissionContainer> {

	private String name;
	private String type;
	
	private List<Group> groups = new ArrayList<Group>();
	private List<PermissionNode> permissions = new ArrayList<PermissionNode>();
	private Map<String, String> info = new LinkedHashMap<String, String>();
	private Map<String, List<Group>> worldGroups = new HashMap<String, List<Group>>();
	private Map<String, List<PermissionNode>> worldPermissions = new HashMap<String, List<PermissionNode>>();
	private Map<String, Map<String, String>> worldInfo = new HashMap<String, Map<String,String>>();
	
	private String description = "";
	private ChatColor color = null;
	private String prefix = null;
	private Map<String, ChatColor> worldColors = new HashMap<String, ChatColor>();
	private Map<String, String> worldPrefixes = new HashMap<String, String>();
	
	private Map<String, List<PermissionNode>> cachedPermissions = new HashMap<String, List<PermissionNode>>();
	private Map<String, Group> cachedPrimaryGroup = new HashMap<String,Group>();
	private Map<String, ChatColor> cachedColor = new HashMap<String,ChatColor>();
	private Map<String, String> cachedPrefix = new HashMap<String,String>();
	
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
	
	public List<PermissionNode> getAllPermissions(String world) {
		if (world == null) world = "";
		if (cachedPermissions.containsKey(world)) {
			return cachedPermissions.get(world);
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
			
			cachedPermissions.put(world, nodes);
			return nodes;
		}
	}
	
	public void fillTrackedNodeList(TrackedNodeList list, String world, boolean addDefaultPerms) {
		if (world == null) world = "";
		
		// add default perms
		if (addDefaultPerms && this instanceof User && ((User)this).isOnline()) {
			Set<Permission> perms = Bukkit.getPluginManager().getDefaultPermissions(((User)this).getPlayer().isOp());
			for (Permission perm : perms) {
				list.add(perm);
			}
		}
		
		// add world perms
		if (!world.isEmpty()) {
			if (worldPermissions.containsKey(world)) {
				for (PermissionNode node : worldPermissions.get(world)) {
					list.add(node, this, world);
				}
			}
		}
		
		// add own perms
		for (PermissionNode node : permissions) {
			list.add(node, this, null);
		}
		
		// add world group perms
		if (!world.isEmpty()) {
			if (worldGroups.containsKey(world)) {
				for (Group group : worldGroups.get(world)) {
					group.fillTrackedNodeList(list, world, false);
				}
			}
		}
		
		// add group perms
		for (Group group : groups) {
			group.fillTrackedNodeList(list, world, false);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getDescription() {
		synchronized (info) {
			return description;
		}
	}
	
	public void setDescription(String desc) {
		synchronized (info) {
			this.description = desc;
			info.put("description", desc);
			dirty = true;
		}
	}
	
	public ChatColor getColor(String world) {
		synchronized (info) {
			if (cachedColor.containsKey(world)) {
				return cachedColor.get(world);
			} else if (worldColors.containsKey(world)) {
				ChatColor c = worldColors.get(world);
				cachedColor.put(world, c);
				return c;
			} else if (color != null) {
				cachedColor.put(world, color);
				return color;
			} else {
				Group group = getPrimaryGroup(world);
				if (group != null) {
					ChatColor c = group.getColor(world);
					if (c != null) {
						cachedColor.put(world, c);
						return c;
					}
				}
			}
			cachedColor.put(world, ChatColor.WHITE);
			return ChatColor.WHITE;
		}
	}
	
	public void setColor(String world, String color) {
		synchronized (info) {
			if (color == null || color.length() == 0) {
				setColor(world, (ChatColor)null);
			} else if (color.length() == 1) {
				setColor(world, ChatColor.getByChar(color));
			} else {
				try {
					setColor(world, ChatColor.valueOf(color.replace(" ", "_").toUpperCase()));
				} catch (IllegalArgumentException e) {
					setColor(world, (ChatColor)null);
				}
			}
		}
	}
	
	public void setColor(String world, ChatColor color) {
		synchronized (info) {
			if (world == null || world.isEmpty()) {
				this.color = color;
				if (color != null) {
					info.put("color", color.name().replace("_", " ").toLowerCase());
				} else {
					info.remove("color");
				}
			} else {
				Map<String, String> wInfo = worldInfo.get(world);
				if (color != null) {
					this.worldColors.put(world, color);
					if (wInfo == null) {
						wInfo = new LinkedHashMap<String, String>();
						worldInfo.put(world, wInfo);
					}
					wInfo.put("color", color.name().replace("_", " ").toLowerCase());
				} else {
					this.worldColors.remove(world);
					if (wInfo != null) {
						wInfo.remove("color");
					}
				}
			}
			cachedColor.clear();
			dirty = true;
		}
	}
	
	public String getPrefix() {
		return getPrefix(null);
	}
	
	public String getPrefix(String world) {
		synchronized (info) {
			if (cachedPrefix.containsKey(world)) {
				return cachedPrefix.get(world);
			} else if (worldPrefixes.containsKey(world)) {
				String p = colorify(worldPrefixes.get(world));
				cachedPrefix.put(world, p);
				return p;
			} else if (prefix != null) {
				String p = colorify(prefix);
				cachedPrefix.put(world, p);
				return p;
			} else {
				Group group = getPrimaryGroup(world);
				if (group != null) {
					String p = group.getPrefix(world);
					if (p != null) {
						cachedPrefix.put(world, p);
						return p;
					}
				}
			}
			cachedPrefix.put(world, "");
			return "";
		}
	}
	
	public void setPrefix(String world, String prefix) {
		synchronized (info) {
			if (prefix != null && !prefix.isEmpty()) {
				prefix = prefix.replace("\u00A7$1", "&");
				if (world == null || world.isEmpty()) {
					this.prefix = prefix;
					info.put("prefix", prefix);
				} else {
					this.worldPrefixes.put(world, prefix);
					Map<String, String> wInfo = worldInfo.get(world);
					if (wInfo == null) {
						wInfo = new LinkedHashMap<String, String>();
						worldInfo.put(world, wInfo);
					}
					wInfo.put("prefix", prefix);
				}
			} else {
				if (world == null || world.isEmpty()) {
					this.prefix = null;
					info.remove("prefix");
				} else {
					this.worldPrefixes.remove(world);
					Map<String, String> wInfo = worldInfo.get(world);
					if (wInfo != null) {
						wInfo.remove("prefix");
					}
				}
			}
			cachedPrefix.clear();
			dirty = true;
		}
	}
	
	public String getInfo(String world, String key) {
		synchronized (info) {
			return info.get(key.toLowerCase());
		}
	}
	
	public void setInfo(String world, String key, String value) {
		synchronized (info) {
			key = key.toLowerCase();
			if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
				value = value.substring(1, value.length() - 1);
			}
			if (key.equals("color")) {
				setColor(world, value);
			} else if (key.equals("prefix")) {
				setPrefix(world, value);
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
	}
	
	public Map<String, String> getActualInfoMap() {
		return info;
	}
	
	public Map<String, Map<String, String>> getActualWorldInfoMap() {
		return worldInfo;
	}
	
	private String colorify(String s) {
		return s.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
	}
	
	public List<PermissionNode> getActualPermissionList() {
		return permissions;
	}
	
	public List<PermissionNode> getActualPermissionList(String world) {
		if (world == null || world.isEmpty()) {
			return permissions;
		} else {
			return worldPermissions.get(world);
		}
	}
	
	public Map<String, List<PermissionNode>> getActualWorldPermissionMap() {
		return worldPermissions;
	}
	
	public List<Group> getActualGroupList() {
		return groups;
	}
	
	public Map<String, List<Group>> getActualWorldGroupMap() {
		return worldGroups;
	}
	
	public List<Group> getActualGroupList(String world) {
		if (world == null || world.isEmpty()) {
			return getActualGroupList();
		} else {
			return worldGroups.get(world);
		}
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
		// check for infinite group recursion
		if (this instanceof Group && group.inheritsGroup(world, (Group)this)) {
			MainPlugin.error("CIRCULAR GROUP REFERENCE DETECTED: while adding " + group.getName() + " to " + this.getName());
			return false;
		}
		if (world == null || world.isEmpty()) {
			if (!groups.contains(group)) {
				groups.add(group);
				dirty = true;
			}
		} else {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups == null) {
				wgroups = new ArrayList<Group>();
				worldGroups.put(world, wgroups);
			}
			if (!wgroups.contains(group)) {
				wgroups.add(group);
				dirty = true;
			}
		}
		return true;
	}
	
	public boolean setGroup(String world, Group group) {
		// check for infinite group recursion
		if (this instanceof Group && group.inheritsGroup(world, (Group)this)) {
			MainPlugin.error("CIRCULAR GROUP REFERENCE DETECTED: while adding " + group.getName() + " to " + this.getName());
			return false;
		}
		if (world == null || world.isEmpty()) {
			groups.clear();
			groups.add(group);
			dirty = true;
		} else {
			List<Group> wgroups = worldGroups.get(world);
			if (wgroups == null) {
				wgroups = new ArrayList<Group>();
				worldGroups.put(world, wgroups);
			}
			wgroups.clear();
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
	
	public boolean inheritsGroup(String world, Group group) {
		List<Group> groups = this.groups;
		if (world != null && !world.isEmpty()) {
			groups = worldGroups.get(world);
		}
		if (groups != null) {
			if (groups.size() == 0) {
				return false;
			} else if (groups.contains(group)) {
				return true;
			} else {
				for (Group g : groups) {
					if (g.inheritsGroup(world, group)) {
						return true;
					}
				}
				return false;
			}
		}
		return false;
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
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty() {
		dirty = true;
	}
	
	public void setNotDirty() {
		dirty = false;
	}
	
	public void clearCache(boolean onlyIfDirty) {
		if (!onlyIfDirty || dirty) {
			cachedPermissions.clear();
			cachedPrimaryGroup.clear();
			cachedColor.clear();
			cachedPrefix.clear();
		}
	}
	
	public void load() {
		MainPlugin.debug("Loading " + type + " '" + name + "'");
		MainPlugin.getStorage().load(this);
	}
	
	public void save() {
		save(false);
	}
	
	public void save(boolean force) {
		if (dirty || force) {
			MainPlugin.debug("Saving " + type + " " + name + " (dirty: " + dirty + ", forced: " + force + ")");
			MainPlugin.getStorage().save(this);
		}
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
