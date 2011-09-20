package com.nisovin.realrp;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.realrp.character.*;
import com.nisovin.realrp.chat.*;
import com.nisovin.realrp.listeners.*;

public class RealRP extends JavaPlugin {

	private static RealRP plugin;
	private Settings settings;
	
	private HashMap<Player,CharacterCreator> characterCreators;
	private ChatManager chatManager;
	private EmoteManager emoteManager;
	
	public static RealRP getPlugin() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		settings = new Settings(this);
		
		characterCreators = new HashMap<Player,CharacterCreator>();
		if (settings.csEnableChatSystem) {
			chatManager = new ChatManager(this);
		}
		if (settings.emEnableEmotes) {
			emoteManager = new EmoteManager();
		}
		//AnimatableNPC.npcManager = new NPCManager(this);
		
		new RPPlayerListener(this);
		
		this.getCommand("rpnpc").setExecutor(new CommandSpawnNpc(this));
		this.getCommand("rpemote").setExecutor(new CommandEmote(this));
		this.getCommand("rpjoinchannel").setExecutor(new CommandJoin(this));
		
		PluginDescriptionFile pdf = getDescription();
		getServer().getLogger().info(pdf.getName() + " v" + pdf.getVersion() + " enabled!");
	}
	
	public static Settings settings() {
		return plugin.settings;
	}
	
	public static void sendMessage(Player player, String message) {
		sendMessage(player, message, (String[])null);
	}
	
	public static String replaceColorCodes(String message) {
		return message.replaceAll("&([0-9a-f])", "\u00A7$1");
	}
	
	public static void sendMessage(Player player, String message, String... replacements) {
		if (replacements != null && replacements.length % 2 == 0) {
			for (int i = 0; i < replacements.length; i+=2) {
				message = message.replace(replacements[i], replacements[i+1]);
			}
		}
		message = replaceColorCodes(message);
		String[] msgs = message.split("\n");
		for (String msg : msgs) {
			player.sendMessage(msg);
		}
	}
	
	public void startCharacterCreator(final Player player) {
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				CharacterCreator cc = new CharacterCreator(player);
				characterCreators.put(player, cc);
			}
		}, 20);
	}
	
	public boolean isCreatingCharacter(Player player) {
		return characterCreators.containsKey(player);
	}
	
	public CharacterCreator getCharacterCreator(Player player) {
		return characterCreators.get(player);
	}
	
	public void finishCharacterCreator(Player player) {
		characterCreators.remove(player);
	}
	
	public void onChat(PlayerChatEvent event) {
		if (chatManager != null) {
			chatManager.onChat(event);
		}
	}
	
	public ChatManager getChatManager() {
		return chatManager;
	}
	
	public EmoteManager getEmoteManager() {
		return emoteManager;
	}
	
	public Emote getEmote(String emote) {
		return emoteManager.getEmote(emote);
	}

	@Override
	public void onDisable() {
		if (chatManager != null) {
			chatManager.turnOff();
		}
	}

}
