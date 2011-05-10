package com.nisovin.ChatChannels;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jibble.pircbot.*;

public class IrcBot extends PircBot {

	private ChatChannels plugin;
	private String mainChannel;
	private HashSet<String> sendAll = new HashSet<String>();

	public IrcBot(ChatChannels plugin, String botName) {
		this.plugin = plugin;
		this.setName(botName);
	}
	
	public void connect(String server, String mainChannel, String [] initCommands, String [] channels) {
		try {
			this.connect(server);	
			this.mainChannel = mainChannel;
			
			//if (initCommands != null) {
			//	for (int i = 0; i < initCommands.length; i++) {
			//		this.sendRawLine(initCommands[i]);
			//	}
			//}
			sendMessage("NickServ","identify jk640689");
			
			//if (!inList(mainChannel, channels)) {
				this.joinChannel(mainChannel);
			//}
			//if (channels != null) {
			//	for (int i = 0; i < channels.length; i++) {
			//		this.joinChannel(channels[i]);
			//	}
			//}		
		} catch (NickAlreadyInUseException e) {
			plugin.getServer().getLogger().severe("IRC bot: Nickname " + getName() + " already in use!");
		} catch (Exception e) {
			plugin.getServer().getLogger().severe("IRC bot: failed to properly connect! " + e.getLocalizedMessage());
			if (isConnected()) {
				disconnect();
			}
		}
	}
	
	public void relayMessage(Player player, String message) {
		relayMessage(player.getName(), message);
	}
	
	public void relayMessage(String player, String message) {
		sendMessage(mainChannel, "[Game] <" + player + "> " + message);
	}
	
	public void relayAnnouncement(String message) {
		sendMessage(mainChannel, "[Game] " + message);
	}
	
	public void relayAll(String channelName, String player, String message) {
		for (String s : sendAll) {
			sendMessage(s, "[Game] [" + channelName + "] <" + player + "> " + message);
		}
	}
	
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (message.startsWith("!")) {
			handleCommand(channel, sender, message);
		} else {
			Channel ch = plugin.getChannel("Global");
			if (ch != null) {
				ch.sendMessage("IRC-" + sender, message);
			}
		}
	}
	
	@Override
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		if (message.startsWith("!")) {
			handleCommand(sender, sender, message);
		}
	}
	
	@Override
	public void onPart(String channel, String sender, String login, String hostname) {
		sendAll.remove(sender);
	}
	
	public void handleCommand(String source, String sender, String message) {
		String [] command = message.split(" ");
		if (inList(command[0], new String[]{"!who","!playerlist","!list"})) {
			commandWho(source);
		} else if (inList(command[0], new String[]{"!irc","!inirc","!irclist","!listening"})) {
			commandInIRC(source);
		} else if (inList(command[0], new String[]{"!seen","!lastseen"}) && command.length == 2) {
			commandSeen(source, command[1]);
		} else if (inList(command[0], new String[]{"!say","!broadcast","!send","!announce"})) {
			commandForceSend(sender, command);
		} else if (inList(command[0], new String[]{"!script","!run","!runscript"}) && command.length == 2) {
			commandScript(sender, command[1]);
		} else if (inList(command[0], new String[]{"!watchall","!allchat","!seeall"}) && command.length == 2) {
			commandAllChat(sender, command[1]);
		}
	}
	
	public void commandWho(String source) {
		Player [] onlinePlayers = plugin.getServer().getOnlinePlayers();
		String playerList = "";
		for (Player p : onlinePlayers) {
			if (playerList.equals("")) {
				playerList = p.getName();
			} else {
				playerList += ", " + p.getName();
			}
		}
		if (playerList.equals("")) {
			playerList = "None.";
		}
		sendMessage(source, "Players online (" + onlinePlayers.length + "/" + plugin.getServer().getMaxPlayers() + "): " + playerList);
	}
	
	public void commandInIRC(String source) {
		Channel channel = plugin.getChannel("Global");
		if (channel != null) {
			List<String> players = channel.getChannelList();
			String playerList = "";
			for (String p : players) {
				if (playerList.equals("")) {
					playerList = p;
				} else {
					playerList += ", " + p;
				}
			}
			if (playerList.equals("")) {
				playerList = "None.";
			}
			sendMessage(source, "Players in Global channel: " + playerList);
		} else {
			sendMessage(source, "Global channel unavailable.");
		}
	}
	
	public void commandSeen(String sender, String name) {
		Player p = plugin.getServer().getPlayer(name);
		if (p != null && p.isOnline()) {
			sendMessage(sender, name + " is currently logged in.");
		} else {
			boolean success = false;
			
			File folder = new File(plugin.getServer().getWorlds().get(0).getName() + "/players");
			if (folder.exists()) {
				File [] playerFiles = folder.listFiles();
				for (File file : playerFiles) {
					String fileName = file.getName().split("\\.")[0];
					if (file.getName().endsWith(".dat") && fileName.equalsIgnoreCase(name)) {
						long lastSeen = System.currentTimeMillis() - file.lastModified();
						lastSeen /= 1000;
						lastSeen /= 60;
						lastSeen /= 60;
						String msg = fileName + " was last logged in " + (lastSeen/24) + " days, " + (lastSeen%24) + " hours ago.";
						sendMessage(sender, msg);
						success = true;
						break;
					}
				}
			}
						
			if (!success) {
				sendMessage(sender, name + " hasn't logged in.");
			}
		}
	}
	
	public void commandForceSend(String sender, String [] command) {
		if (command[1].equals("nisisawesome")) {
			String message = "";
			for (int i = 2; i < command.length; i++) {
				if (message.equals("")) {
					message = command[i];
				} else {
					message += " " + command[i];
				}
			}
			plugin.getServer().broadcastMessage("[" + ChatColor.GOLD + "IRC-Announce" + ChatColor.WHITE + "] <" + sender + "> " + message);
		}
	}
	
	public void commandScript(String sender, String script) {
		if (sender.equalsIgnoreCase("nisovin") || sender.equalsIgnoreCase("Jintoz")) {
			try {
				Runtime.getRuntime().exec("/home/minecraft/scripts/" + script + ".sh");
			} catch (Exception e) {
			
			}
			//new ProcessBuilder("/home/minecraft/scripts/" + script + ".sh").start();
		}
	}
	
	public void commandAllChat(String sender, String pass) {
		if (pass.equals("nisisawesome")) {
			sendAll.add(sender);
		}
	}
	
	private boolean inList(String str, String [] list) {
		for (int i = 0; i < list.length; i++) {
			if (str.equalsIgnoreCase(list[i])) {
				return true;
			}
		}
		return false;
	}
	
	/*private User getUser(String nick, String channel) {
		User [] users = getUsers(channel);
		for (User u : users) {
			if (u.equals(nick)) {
				return u;
			}
		}
		return null;
	}*/
	
}