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
	
	/**
	 * Gets the Player object for this User. If the player is offline, this will
	 * return null instead.
	 * @return The Player object, or null if offline.
	 */
	public Player getPlayer() {
		return Bukkit.getPlayerExact(realName);
	}
	
	/**
	 * Gets the OfflinePlayer object for this User.
	 * @return The OfflinePlayer object.
	 */
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(realName);
	}
	
	/**
	 * Checks whether this player is online.
	 * @return True if online.
	 */
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
	
	@Override
	public String getPrefix() {
		Player p = getPlayer();
		if (p != null && p.isOnline()) {
			return getPrefix(p.getWorld().getName());
		} else {
			return getPrefix(null);
		}
	}
	
}
