package com.nisovin.ChatChannels;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatChannels extends JavaPlugin {

	public static final int YELL_RANGE = 200;
	public static final int YELL_COOLDOWN = 60;
	
	public static Server server;
	public static HashMap<String, Channel> channels = new HashMap<String, Channel>();
	public static HashMap<String, Channel> activeChannels = new HashMap<String, Channel>();
	public static HashMap<String, Long> lastYell = new HashMap<String, Long>();
	public static IrcBot ircBot;

	@Override
	public void onEnable() {
		server = getServer();
		
		ircBot = new IrcBot(this, "HatuBot");
		ircBot.connect("irc.esper.net", "#hatu", new String[]{"/msg NickServ identify jk640689"}, null);
		
		Channel global = new Channel("Global", "", ChatColor.AQUA, false);
		channels.put("global", global);
		
		Channel local = new Channel("Local", "", ChatColor.GOLD, false);
		channels.put("local", local);
		
		Channel admin = new Channel("Admin", "pass", ChatColor.YELLOW, false);
		channels.put("admin", admin);
		
		/*Channel irc = null;
		if (ircBot != null && ircBot.isConnected()) {
			getServer().getLogger().info("Creating IRC channel");
			irc = new Channel("IRC", "", ChatColor.GREEN, false);
			channels.put("irc", irc);
		}*/
		
		for (Player p : getServer().getOnlinePlayers()) {
			local.join(p);
			p.sendMessage("You have joined the '" + local.getColoredName() + "' channel.");
			activeChannels.put(p.getName(), local);
			
			global.join(p);
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
				if ((args.length == 2 || args.length == 3) && args[0].equalsIgnoreCase("join")) {
					commandJoin(p, args[1], (args.length==3?args[2]:""));
				} else if (args.length == 2 && args[0].equalsIgnoreCase("leave")) {
					commandLeave(p, args[1]);
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
				} else {
					p.sendMessage("Usage of /channel command:");
					p.sendMessage("   /channel join <channel> [pass] -- join/create a channel");
					p.sendMessage("   /channel leave <channel> -- leave a channel");
					p.sendMessage("   /channel set <channel> -- set your active channel");
					p.sendMessage("   /channel list -- lists your channels");
					p.sendMessage("   /channel all -- lists all channels");
				}
				return true;
			} else if (command.getName().equalsIgnoreCase("join")) {
				if (args.length == 1 || args.length == 2) {
					commandJoin(p, args[0], (args.length==2?args[1]:""));
				} else {
					p.sendMessage("Usage: /join <channel> [pass]");
				}
				return true;
			} else if (command.getName().equalsIgnoreCase("leave")) {
				if (args.length == 1) {
					commandLeave(p, args[0]);
				} else {
					p.sendMessage("Usage: /leave <channel>");
				}
				return true;
			} else if (command.getName().equalsIgnoreCase("yell")) {
				commandYell(p, args);
				return true;
			}
		}
		
		return false;
	}
	
	public void commandJoin(Player p, String chan, String pass) {
		Channel channel = channels.get(chan.toLowerCase());
		if (channel != null) {
			if (channel.isInChannel(p)) {
				activeChannels.put(p.getName(), channel);
				p.sendMessage("You are now talking in the '" + channel.getColoredName() + "' channel.");
			} else {
				boolean joined = channel.join(p, pass);
				if (joined) {
					activeChannels.put(p.getName(), channel);
					p.sendMessage("You have joined the '" + channel.getColoredName() + "' channel.");
				} else {
					p.sendMessage("Unable to join that channel.");
				}
			}
		} else {
			channel = new Channel(chan, pass);
			channel.join(p, pass);
			channels.put(chan.toLowerCase(), channel);
			activeChannels.put(p.getName(), channel);
			p.sendMessage("You have created the '" + channel.getColoredName() + "' channel.");
		}
		
	}
	
	public void commandLeave(Player p, String chan) {
		Channel channel = channels.get(chan.toLowerCase());
		if (channel != null) {
			if (channel.isInChannel(p)) {
				channel.leave(p);
				if (activeChannels.get(p.getName()).equals(channel)) {
					activeChannels.remove(p.getName());
					Channel global = getChannel("Global");
					if (global.isInChannel(p)) {
						activeChannels.put(p.getName(), global);
					}
				}
				p.sendMessage("You have left the '" + channel.getColoredName() + "' channel.");
			} else {
				p.sendMessage("You are not in that channel.");
			}
		} else {
			p.sendMessage("You are not in that channel.");
		}
	}
	
	public void commandYell(Player player, String [] args) {
		Long last = lastYell.get(player.getName()); 
		if (args.length == 0) {
			player.sendMessage("Nothing to yell!");
		} else if (last != null && last + (YELL_COOLDOWN*1000) > System.currentTimeMillis()) {
			player.sendMessage("Your throat hurts, you should wait a bit before yelling again.");
		} else {
			String msg = "";
			for (String s : args) {
				msg += s + " ";
			}
			Player[] online = getServer().getOnlinePlayers();
			int c = 0;
			for (Player p : online) {
				if (Math.abs(p.getLocation().getX() - player.getLocation().getX()) < YELL_RANGE &&
						Math.abs(p.getLocation().getY() - player.getLocation().getY()) < YELL_RANGE && 
						Math.abs(p.getLocation().getZ() - player.getLocation().getZ()) < YELL_RANGE) {
					p.sendMessage("[" + ChatColor.RED + "Yell" + ChatColor.WHITE + "] <" + player.getName() + "> " + msg);
					c++;
				}				
			}
			if (c <= 1) {
				player.sendMessage("Nobody heard your shout.");
			}
			lastYell.put(player.getName(), System.currentTimeMillis());
		}		
	}
	
	public void commandSet(Player p, String [] args) {
		
	}
	
	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}
	
	/*private void loadChannelList() {
		File folder = getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
				
		File file = new File(folder, "channels.txt");
		try {
			Scanner scanner = new Scanner(file);
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					flagged.add(line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {			
		}
	}
	
	private void saveChannelList() {
		File file = new File(getDataFolder(), "channels.txt");
		
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
			
			HashMap<String,HashSet<String>> playerData = new HashMap<String,HashSet<String>>();
			
			for (String s : channels.keySet()) {
				Channel c = channels.get(s);
				if (!c.alwaysMaintain() && c.getChannelList().size() > 0) {
					String line = "channel:" + c.getName();
					if (c.hasPassword()) {
						line += ":" + c.getPassword();
					}
					writer.append(line);
					writer.newLine();
					
					//List<String> list = c.
					//for (String p : list) {
						
					//}
				}
			}
			
			writer.close();
			
		} catch (IOException e) {
		}		
	}*/
	
	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			p.sendMessage("You have left all channels (plugin unloaded).");
		}
		if (ircBot != null && ircBot.isConnected()) {
			ircBot.quitServer("Chat plugin unloaded.");
		}
		
	}

}
