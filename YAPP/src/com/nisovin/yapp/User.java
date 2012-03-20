package com.nisovin.yapp;

import org.bukkit.entity.Player;

public class User extends PermissionContainer {

	private String realName;
	
	public User(Player player) {
		super(player.getName().toLowerCase(), "player");
		this.realName = player.getName();
	}
	
	public User(String player) {
		super(player.toLowerCase(), "player");
		this.realName = player;
	}
	
	@Override
	public String getName() {
		return realName;
	}
	
}
