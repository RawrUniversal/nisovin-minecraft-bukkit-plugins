package com.nisovin.yapp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
	
	public Player getPlayer() {
		return Bukkit.getPlayerExact(realName);
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(realName);
	}
	
	public boolean isOnline() {
		Player p = getPlayer();
		return p != null && p.isOnline();
	}
	
	@Override
	public void setColor(String world, ChatColor color) {
		super.setColor(world, color);
		Player p = getPlayer();
		if (p != null) {
			YAPP.plugin.setPlayerListName(p, this);
		}
	}
	
}
