package com.nisovin.realrp.chat;

import org.bukkit.Bukkit;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

public class IrcBot extends PircBot {

	private ChatManager cm;
	private String server;
	private String channel;
	private String nickservPass;
	
	public IrcBot(ChatManager cm, String server, String name, String channel, String nickservPass) {
		this.cm = cm;
		this.server = server;
		this.channel = channel;
		this.nickservPass = nickservPass;
		this.setName(name);
		
		connect();
	}
	
	public void connect() {
		try {
			this.connect(server);
			
			if (nickservPass != null && !nickservPass.isEmpty()) {
				sendMessage("NickServ","identify " + nickservPass);				
			}
			
			this.joinChannel(channel);
		} catch (NickAlreadyInUseException e) {
			Bukkit.getServer().getLogger().severe("RealRP: IRC bot: Nickname " + getName() + " already in use!");
		} catch (Exception e) {
			Bukkit.getServer().getLogger().severe("RealRP: IRC bot: failed to properly connect! " + e.getLocalizedMessage());
			if (isConnected()) {
				disconnect();
			}
		}		
	}
	
	public void sendMessage(String message) {
		sendMessage(channel, message);
	}
	
	@Override
	public void onMessage(String channel, String sender, String login, String hostname, String message) {
		cm.fromIRC(sender, message);
	}
	
}
