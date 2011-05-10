package com.nisovin.ChatChannels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
		sendMessage(from.getDisplayName(), message);
	}
	
	public void sendMessage(String from, String message) {
		String msg = ChatColor.WHITE + "[" + color + name + ChatColor.WHITE + "] <" + from + ChatColor.WHITE + "> " + message;
		if (!this.name.equals("Local")) {
			// normal chat (including IRC)
			for (String s : playersInChannel) {
				Player p = ChatChannels.server.getPlayer(s);
				if (p != null && p.isOnline()) {				
					p.sendMessage(msg);
				}
			}
		} else {
			// local chat only
			int range = 50;
			Player player = ChatChannels.server.getPlayer(from);
			Player[] allPlayers = ChatChannels.server.getOnlinePlayers();
			ArrayList<Player> sendTo = new ArrayList<Player>();
			for (Player p : allPlayers) {
				if (playersInChannel.contains(p.getName()) &&
						Math.abs(p.getLocation().getX() - player.getLocation().getX()) < range &&
						Math.abs(p.getLocation().getY() - player.getLocation().getY()) < range && 
						Math.abs(p.getLocation().getZ() - player.getLocation().getZ()) < range) {
					sendTo.add(p);
				}
			}
			for (Player p : sendTo) {
				p.sendMessage(msg);
			}
			if (sendTo.size() == 1) {
				player.sendMessage("Nobody heard you! Type '/join Global' to join global chat.");
			}
		}
		ChatChannels.server.getLogger().info(ChatColor.stripColor(msg));
		IrcBot bot = ChatChannels.ircBot;
		if (bot != null && bot.isConnected()) {
			// irc chat
			if (this.name.equals("Global") && !from.startsWith("IRC-")) {
				bot.relayMessage(from, message);
			}
			bot.relayAll(name, from, message);
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
		if (destroyOnEmpty && getChannelList().size() == 0) {
			// TODO: delete channel
			//playersInChannel.clear();
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
	
	public String getPassword() {
		return password;
	}
	
	public boolean hasPassword() {
		return !password.equals("");
	}
	
	public boolean alwaysMaintain() {
		return !destroyOnEmpty;
	}
	
	public List<String> getChannelList() {
		ArrayList<String> inChannel = new ArrayList<String>();
		for (String s : playersInChannel) {
			Player p = ChatChannels.server.getPlayer(s);
			if (p != null && p.isOnline()) {
				inChannel.add(s);
			}
		}
		return inChannel;
	}
	
}
