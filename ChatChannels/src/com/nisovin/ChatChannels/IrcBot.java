import org.jibble.pircbot.*;

public class IrcBot extends PircBot {

	private ChatChannels plugin;
	private String mainChannel;

	public IrcBot(ChatChannels plugin, String botName) {
		this.plugin = plugin;
		this.setName(botName);
		bot = this;
	}
	
	public void connect(String server, String mainChannel, String [] channels) {
		this.connect(server);
		this.mainChannel = mainChannel;
		if (!inList(mainChannel, channels)) {
			this.joinChannel(mainChannel);
		}
		if (channels != null) {
			for (int i = 0; i < channels.length; i++) {
				this.joinChannel(channels[i]);
			}
		}
	}
	
	public void relayMessage(Player player, String message) {
		relayMessage(player.getName(), message);
	}
	
	public void relayMessage(String player, String message) {
		sendMessage(mainChannel, "[Game] <" + player + "> " + message);
	}
	
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (message.startsWith("!")) {
			handleCommand(channel, sender, message);
		} else {
			Channel channel = plugin.getChannel("IRC");
			if (channel != null) {
				channel.sendMessage("IRC-" + sender, message);
			}
		}
	}
	
	public void onPrivateMessage(String sender, String login, String hostname, String message) {
		if (message.startsWith("!")) {
			handleCommand(sender, sender, message);
		}
	}
	
	public void handleCommand(String source, String sender, String message) {
		String [] command = message.split(" ");
		if (inList(command[0], new String[]{"!who","!playerlist","!list"})) {
			commandWho(source);
		} else if (inList(command[0], new String[]{"!inirc","!irclist","!listening"})) {
			commandInIRC(source);
		} else if (inList(command[0], new String[]{"!seen","!lastseen"}) && command.length == 2) {
			commandSeen(source, command[1]);
		} else if (inList(command[0], new String[]{"!say","!broadcast"})) {
			commandForceSend(sender, command);
		} else if (inList(command[0], new String[]{"!script","!run","!runscript"}) && command.length == 2) {
			commandScript(sender, command[1]);
		}
	}
	
	public void commandWho(String source) {
		List<Player> onlinePlayers = plugin.getServer().getOnlinePlayers();
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
		sendMessage(source, "Players online (" + onlinePlayers.size() + "/" + plugin.getServer().getMaxPlayers() + "): " + playerList);
	}
	
	public void commandInIRC(String source) {
		Channel channel = plugin.getChannel("IRC");
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
			sendMessage(source, "Players in IRC channel: " + playerList;
		} else {
			sendMessage(source, "IRC channel unavailable.");
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
				File [] playerFiles = folder.listFiles("*.dat");
				for (File file : playerFiles) {
					String fileName = file.getName().split(".")[0];
					if (fileName.equalsIgnoreCase(name)) {
						long lastSeen = System.currentTimeMillis() - file.lastModified();
						lastSeen /= 1000;
						lastSeen /= 60;
						lastSeen /= 60;
						String msg = fileName + " was last logged in " + (lastSeen%24) + " days, " + (lastSeen/24) + " hours ago.";
						sendMessage(sender, msg);
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
			plugin.getServer().broadcastMessage("[IRC-Announce] <" + sender + "> " + message);
		}
	}
	
	public void commandScript(String sender, String script) {
		if (sender.equalsIgnoreCase("nisovin") || sender.equalsIgnoreCase("Jintoz")) {
			//Runtime.getRuntime().exec("/home/minecraft/scripts/" + script + ".sh");
			//new ProcessBuilder("/home/minecraft/scripts/" + script + ".sh").start();
		}
	}
	
	private boolean inList(String str, String [] list) {
		for (int i = 0; i < list; i++) {
			if (str.equalsIgnoreCase(list[i])) {
				return true;
			}
		}
		return false;
	}
	
	private User getUser(String nick, String channel) {
		User [] users = getUsers(channel);
		for (User u : users) {
			if (u.equals(nick)) {
				return u;
			}
		}
		return null;
	}
	
}