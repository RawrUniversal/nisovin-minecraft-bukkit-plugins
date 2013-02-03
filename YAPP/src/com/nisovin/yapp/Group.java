package com.nisovin.yapp;

import org.bukkit.entity.Player;

public class Group extends PermissionContainer {

	public Group(String name) {
		super(name, "group");
	}
	
	public Group(Group group, String name) {
		super(group, name);
	}
	
	/**
	 * Adds the specified player to this group, at the server level.
	 * @param player The player to add.
	 */
	public void addPlayer(Player player) {
		addPlayer(player.getName());
	}
	
	/**
	 * Adds the specified player to this group, at the server level.
	 * @param playerName The name of the player to add.
	 */
	public void addPlayer(String playerName) {
		User user = YAPP.getPlayerUser(playerName);
		user.addGroup(this);
	}
	
	/**
	 * Adds the specified player to this group, for the specified world.
	 * @param world The applicable world.
	 * @param player The player to add.
	 */
	public void addPlayer(String world, Player player) {
		addPlayer(world, player.getName());
	}
	
	/**
	 * Adds the specified player to this group, for the specified world.
	 * @param world The applicable world.
	 * @param playerName The name of the player to add.
	 */
	public void addPlayer(String world, String playerName) {
		User user = YAPP.getPlayerUser(playerName);
		user.addGroup(world, this);
	}
	
}
