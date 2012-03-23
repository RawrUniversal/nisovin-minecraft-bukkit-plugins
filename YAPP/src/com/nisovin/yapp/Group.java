package com.nisovin.yapp;

import org.bukkit.entity.Player;

public class Group extends PermissionContainer {

	public Group(String name) {
		super(name, "group");
	}
	
	public Group(Group group, String name) {
		super(group, name);
	}
	
	public void addPlayer(Player player) {
		addPlayer(player.getName());
	}
	
	public void addPlayer(String player) {
		User user = MainPlugin.getPlayerUser(player);
		user.addGroup(this);
	}
	
}
