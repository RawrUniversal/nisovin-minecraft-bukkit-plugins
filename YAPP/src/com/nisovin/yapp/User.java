package com.nisovin.yapp;

import org.bukkit.entity.Player;

public class User extends PermissionContainer {

	public User(Player player) {
		super(player.getName().toLowerCase(), "player");
	}
	
	public User(String player) {
		super(player.toLowerCase(), "player");
	}
	
}
