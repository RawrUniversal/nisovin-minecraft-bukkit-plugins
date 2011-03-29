package com.nisovin.ChatChannels;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Channel {

	private String name;
	private String password;
	private ChatColor color;
	private boolean destroyOnEmpty;
	private HashSet<String> playersInChannel;
	
	public Channel(String name, String password) {
		this(name, password, ChatColor.BLUE, true);
	}
	
	public Channel(String name, String password, ChatColor color) {
		this(name, password, color, true);
	}
	
	public Channel(String name, String password, ChatColor color, boolean destroyOnEmpty) {
		this.name = name;
		this.password = password;
		this.color = color;
		this.destroyOnEmpty = destroyOnEmpty;
		this.playersInChannel = new HashSet<String>();
	}
	
	public void sendMessage(Player from, String message) {
		for (String s : playersInChannel) {
			Player p = ChatChannels.server.getPlayer(s);
			if (p != null && p.isOnline()) {
				String msg = ChatColor.WHITE + "[" + color + name + ChatColor.WHITE + "] <" + from.getName() + "> " + message;
				p.sendMessage(msg);
				ChatChannels.server.getLogger().info(ChatColor.stripColor(msg));
			}
		}
	}
	
	public boolean join(Player player) {
		return join(player, "");
	}
	
	public boolean join(Player player, String password) {
		if (password.equals(this.password)) {
			playersInChannel.add(player.getName());
			return true;
		} else {
			return false;
		}
	}
	
	public void forceJoin(Player player) {
		playersInChannel.add(player.getName());
	}
	
	public void leave(Player player) {
		playersInChannel.remove(player.getName());
		if (destroyOnEmpty && playersInChannel.size() == 0) {
			// TODO
		}
	}
	
	public boolean isInChannel(Player player) {
		return playersInChannel.contains(player.getName());
	}
	
	public String getName() {
		return name;
	}
	
	public String getColoredName() {
		return color + name + ChatColor.WHITE;
	}
	
}
