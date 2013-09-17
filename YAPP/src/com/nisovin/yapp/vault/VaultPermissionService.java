package com.nisovin.yapp.vault;

import java.util.Set;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.YAPP;

public class VaultPermissionService extends net.milkbowl.vault.permission.Permission {
	
	@Override
	public String getName() {
		return "YAPP";
	}

	@Override
	public boolean isEnabled() {
		return YAPP.plugin != null && YAPP.plugin.isEnabled();
	}

	@Override
	public boolean hasSuperPermsCompat() {
		return true;
	}

	@Override
	public boolean playerHas(String world, String player, String permission) {
		return YAPP.getPlayerUser(player).has(world, permission);
	}

	@Override
	public boolean playerAdd(String world, String player, String permission) {
		return YAPP.getPlayerUser(player).addPermission(world, permission);
	}

	@Override
	public boolean playerRemove(String world, String player, String permission) {
		return YAPP.getPlayerUser(player).removePermission(world, permission);
	}

	@Override
	public boolean groupHas(String world, String group, String permission) {
		Group g = YAPP.getGroup(group);
		if (g != null) {
			return g.has(world, permission);
		} else {
			return false;
		}
	}

	@Override
	public boolean groupAdd(String world, String group, String permission) {
		Group g = YAPP.getGroup(group);
		if (g != null) {
			return g.addPermission(world, permission);
		} else {
			return false;
		}
	}

	@Override
	public boolean groupRemove(String world, String group, String permission) {
		Group g = YAPP.getGroup(group);
		if (g != null) {
			return g.removePermission(world, permission);
		} else {
			return false;
		}
	}

	@Override
	public boolean playerInGroup(String world, String player, String group) {
		Group g = YAPP.getGroup(group);
		if (g == null) {
			return false;
		} else {
			return YAPP.getPlayerUser(player).inGroup(world, g, true);
		}
	}

	@Override
	public boolean playerAddGroup(String world, String player, String group) {
		Group g = YAPP.getGroup(group);
		if (g == null) {
			g = YAPP.newGroup(group);
		}
		return YAPP.getPlayerUser(player).addGroup(world, g);
	}

	@Override
	public boolean playerRemoveGroup(String world, String player, String group) {
		Group g = YAPP.getGroup(group);
		if (g == null) {
			return false;
		} else {
			return YAPP.getPlayerUser(player).removeGroup(world, g);
		}
	}

	@Override
	public String[] getPlayerGroups(String world, String player) {
		Set<Group> groups = YAPP.getPlayerUser(player).getGroups(world);
		Group[] groupsArray = groups.toArray(new Group[groups.size()]);
		String[] groupNames = new String[groups.size()];
		for (int i = 0; i < groupsArray.length; i++) {
			groupNames[i] = groupsArray[i].getName();
		}
		return groupNames;
	}

	@Override
	public String getPrimaryGroup(String world, String player) {
		Group group = YAPP.getPlayerUser(player).getPrimaryGroup(world);
		if (group == null) {
			return null;
		} else {
			return group.getName();
		}
	}	
	
	@Override
	public String[] getGroups() {
		return YAPP.getGroupNames().toArray(new String[]{});
	}

	@Override
	public boolean hasGroupSupport() {
		return true;
	}

}
