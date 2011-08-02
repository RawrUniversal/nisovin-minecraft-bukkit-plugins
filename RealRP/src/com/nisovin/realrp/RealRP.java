package com.nisovin.realrp;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.martin.bukkit.npclib.NPCManager;

import com.nisovin.realrp.character.*;
import com.nisovin.realrp.chat.*;
import com.nisovin.realrp.listeners.RPPlayerListener;

public class RealRP extends JavaPlugin {

	private static RealRP plugin;
	private Settings settings;
	
	private HashMap<Player,CharacterCreator> characterCreators;
	private EmoteManager emotes;
	
	public static RealRP getPlugin() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		plugin = this;
		
		settings = new Settings();
		
		characterCreators = new HashMap<Player,CharacterCreator>();
		emotes = new EmoteManager();
		AnimatableNPC.npcManager = new NPCManager(this);
		
		new RPPlayerListener(this);
		
		this.getCommand("spawnnpc").setExecutor(new CommandSpawnNpc(this));
	}
	
	public static Settings settings() {
		return plugin.settings;
	}
	
	public static void sendMessage(Player player, String message) {
		sendMessage(player, message, (String[])null);
	}
	
	public static void sendMessage(Player player, String message, String... replacements) {
		if (replacements != null && replacements.length % 2 == 0) {
			for (int i = 0; i < replacements.length; i+=2) {
				message.replace(replacements[i], replacements[i+1]);
			}
		}
		message = message.replaceAll("&([0-9a-f])", "\u00A7$1");
		String[] msgs = message.split("\n");
		for (String msg : msgs) {
			player.sendMessage(msg);
		}
	}
	
	public void startCharacterCreator(Player player) {
		CharacterCreator cc = new CharacterCreator(player);
		characterCreators.put(player, cc);
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
	
	public EmoteManager getEmoteManager() {
		return emotes;
	}
	
	public Emote getEmote(String emote) {
		return emotes.getEmote(emote);
	}

	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
