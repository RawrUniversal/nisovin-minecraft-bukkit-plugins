package com.nisovin.realrp.chat;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import com.nisovin.realrp.RealRP;

public class IrcBot extends PircBot {

	private ChatManager cm;
	private String server;
	private String channel;
	private String nickservPass;
	private String authPass;
	
	private HashMap<String, IrcCommandSender> authed = new HashMap<String, IrcCommandSender>();
	
	public IrcBot(ChatManager cm, String server, String name, String channel, String nickservPass, String authPass) {
		this.cm = cm;
		this.server = server;
		this.channel = channel;
		this.nickservPass = nickservPass;
		this.authPass = authPass;
		this.setName(name);
		
		//new Thread(new IrcConnector(this)).run();
		Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(RealRP.getPlugin(), new IrcConnector(this), 1, 60*20);
	}
	
	public void connect() {
		try {
			this.connect(server);
			
			if (nickservPass != null && !nickservPass.isEmpty()) {
				sendMessage("NickServ","identify " + nickservPass);				
			}
			
			this.joinChannel(channel);
			
			Bukkit.getServer().getLogger().info("RealRP: IRC bot " + getName() + " is connected.");
		} catch (NickAlreadyInUseException e) {
			Bukkit.getServer().getLogger().severe("RealRP: IRC bot: Nickname " + getName() + " already in use!");
		} catch (Exception e) {
			Bukkit.getServer().getLogger().severe("RealRP: IRC bot: failed to properly connect! " + e.getLocalizedMessage());
			if (isConnected()) {
				disconnect();
			}
		}		
	}
	
	public void forceReconnect() {
		if (isConnected()) {
			disconnect();
		}
		connect();
	}
	
	public void sendMessage(String message) {
		sendMessage(channel, message);
	}
	
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		if (cm.settings.csIRCWhoCommand.equals(message)) {
			Player[] players = Bukkit.getServer().getOnlinePlayers();
			String msg = "Online players (" + players.length + "): ";
			if (players.length == 0) {
				msg += "None.";
			} else {
				for (int i = 0; i < players.length; i++) {
					msg += (i==0?" ":", ") + ChatColor.stripColor(players[i].getDisplayName());
				}
			}
			sendMessage(channel, msg);
		} else {
			cm.fromIRC(sender, message);
		}
	}
	
	@Override
	protected void onPrivateMessage(String sender, String login, String hostname, String message) {
		if (!authPass.isEmpty()) {
			if (message.equals("auth " + authPass)) {
				authed.put(sender, new IrcCommandSender(this, sender));
				sendMessage(sender, "Password accepted. You are now authorized.");
			} else if (authed.containsKey(sender) && (message.equals("auth") || message.equals("deauth"))) {
				authed.remove(sender);
				sendMessage(sender, "You are no longer authorized.");
			} else {
				IrcCommandSender commSender = authed.get(sender);
				if (commSender != null) {
					Bukkit.getServer().dispatchCommand(commSender, message);
				}
			}
		}
	}	

	@Override
	protected void onPart(String channel, String sender, String login, String hostname) {
		authed.remove(sender);
	}

	private class IrcConnector implements Runnable {

		private IrcBot bot;
		
		public IrcConnector(IrcBot bot) {
			this.bot = bot;
		}
		
		@Override
		public void run() {
			if (!bot.isConnected()) {
				bot.connect();
			}
		}
		
	}
	
	public class IrcCommandSender extends ConsoleCommandSender {

		private IrcBot bot;
		private String name;
		
		public IrcCommandSender(IrcBot bot, String name) {
			super(Bukkit.getServer());
			this.bot = bot;
			this.name = name;
		}

		@Override
		public boolean isOp() {
			return true;
		}

		@Override
		public void sendMessage(String message) {
			bot.sendMessage(name, ChatColor.stripColor(message));
		}
		
	}
	
}
