package com.nisovin.ChatChannels;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatChannels extends JavaPlugin {

	public static Server server;
	public HashMap<String, Channel> channels = new HashMap<String, Channel>();
	public HashMap<String, Channel> activeChannels = new HashMap<String, Channel>();

	@Override
	public void onEnable() {
		server = getServer();
		
		Channel global = new Channel("Global", "", ChatColor.AQUA, false);
		Channel admin = new Channel("Admin", "pass", ChatColor.YELLOW, false);
		channels.put("global", global);
		channels.put("admin", admin);
		
		for (Player p : getServer().getOnlinePlayers()) {
			global.join(p);
			activeChannels.put(p.getName(), global);
			p.sendMessage("You have joined the '" + global.getColoredName() + "' channel.");
			if (p.isOp()) {
				admin.forceJoin(p);
				p.sendMessage("You have joined the '" + admin.getColoredName() + "' channel.");
			}
		}
		
		CCPlayerListener playerListener = new CCPlayerListener(this);
		
		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.High, this);
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (command.getName().equalsIgnoreCase("channel")) {
				if (args.length >= 2 && args[0].equalsIgnoreCase("join")) {
					Channel channel = channels.get(args[1].toLowerCase());
					if (channel != null) {
						String pass = "";
						for (int i = 2; i < args.length; i++) {
							pass += args[i] + " ";
						}
						pass = pass.trim();
						boolean joined = channel.join(p, pass);
						if (joined) {
							activeChannels.put(p.getName(), channel);
							p.sendMessage("You have joined the '" + channel.getColoredName() + "' channel.");
						} else {
							p.sendMessage("Unable to join that channel.");
						}
					} else {
						String pass = "";
						for (int i = 2; i < args.length; i++) {
							pass += args[i] + " ";
						}
						pass = pass.trim();
						channel = new Channel(args[1], pass);
						channel.join(p, pass);
						channels.put(args[1].toLowerCase(), channel);
						activeChannels.put(p.getName(), channel);
						p.sendMessage("You have created the '" + channel.getColoredName() + "' channel.");
					}
				} else if (args.length == 2 && args[0].equalsIgnoreCase("leave")) {
					Channel channel = channels.get(args[1].toLowerCase());
					if (channel != null) {
						if (channel.isInChannel(p)) {
							channel.leave(p);
							p.sendMessage("You have left the '" + channel.getColoredName() + "' channel.");
						} else {
							p.sendMessage("You are not in that channel.");
						}
					} else {
						p.sendMessage("You are not in that channel.");
					}
				} else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
					Channel channel = channels.get(args[1].toLowerCase());
					if (channel != null && channel.isInChannel(p)) {
						activeChannels.put(p.getName(), channel);
						p.sendMessage("You are now talking in the '" + channel.getColoredName() + "' channel.");
					} else {
						p.sendMessage("Unable to set active channel.");
					}
				} else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
					String list = "";
					for (Channel channel : channels.values()) {
						if (channel.isInChannel(p)) {
							if (list.equals("")) {
								list = channel.getColoredName();
							} else {
								list += ", " + channel.getColoredName();
							}
						}
					}
					p.sendMessage("You are in channels: " + list);
				}
				return true;
			}
		}
		
		return false;
	}
	
	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}
	
	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			p.sendMessage("You have left all channels (plugin unloaded).");
		}
		
	}

}
