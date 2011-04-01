package com.nisovin.ChatChannels;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatChannels extends JavaPlugin {

	public static Server server;
	public HashMap<String, Channel> channels = new HashMap<String, Channel>();
	public HashMap<String, Channel> activeChannels = new HashMap<String, Channel>();
	public static IrcBot ircBot;

	@Override
	public void onEnable() {
		server = getServer();
		
		try {
			ircBot = new IrcBot(this, "NislandBot");
			ircBot.connect("irc.esper.net","#nisland",null);
		} catch (IrcException e) {
		}
		
		Channel global = new Channel("Global", "", ChatColor.AQUA, false);
		channels.put("global", global);
		
		Channel admin = new Channel("Admin", "pass", ChatColor.YELLOW, false);
		channels.put("admin", admin);
		
		if (ircBot.isConnected()) {
			Channel irc = new Channel("IRC", "", ChatColor.GREEN, false);
			channels.put("irc", irc);
		}
		
		for (Player p : getServer().getOnlinePlayers()) {
			global.join(p);
			activeChannels.put(p.getName(), global);
			p.sendMessage("You have joined the '" + global.getColoredName() + "' channel.");
			if (p.isOp()) {
				admin.forceJoin(p);
				p.sendMessage("You have joined the '" + admin.getColoredName() + "' channel.");
			}
		}
		
		new CCPlayerListener(this);
		
		this.getServer().getLogger().info("ChatChannels plugin loaded!");
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
						int count = channel.getChannelList().size();
						if (channel.isInChannel(p)) {
							if (list.equals("")) {
								list = channel.getColoredName() + " (" + count + ")";
							} else {
								list += ", " + channel.getColoredName() + " (" + count + ")";
							}
						}
					}
					p.sendMessage("You are in channels: " + list);
				} else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
					String list = "";
					for (Channel channel : channels.values()) {
						int count = channel.getChannelList().size();
						if (!channel.hasPassword() && (count > 0 || channel.alwaysMaintain())) {
							if (list.equals("")) {
								list = channel.getColoredName() + " (" + count + ")";
							} else {
								list += ", " + channel.getColoredName() + " (" + count + ")";
							}
						}
					}
					p.sendMessage("Available channels: " + list);
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
