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
	
	@Override
	public void setColor(ChatColor color) {
		super.setColor(color);
		Player p = getPlayer();
		if (p != null) {
			MainPlugin.yapp.setPlayerListName(p, this);
		}
	}
	
}
