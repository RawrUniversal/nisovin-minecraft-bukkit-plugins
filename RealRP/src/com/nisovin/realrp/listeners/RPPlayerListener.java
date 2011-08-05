package com.nisovin.realrp.listeners;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import com.nisovin.realrp.RealRP;
import com.nisovin.realrp.character.CharacterCreator;
import com.nisovin.realrp.character.PlayerCharacter;
import com.nisovin.realrp.chat.Emote;

public class RPPlayerListener extends PlayerListener {

	private RealRP plugin;
	
	public RPPlayerListener(RealRP plugin) {
		this.plugin = plugin;
		
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this, Event.Priority.Highest, plugin);
		pm.registerEvent(Event.Type.PLAYER_CHAT, this, Event.Priority.High, plugin);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		PlayerCharacter pc = PlayerCharacter.get(event.getPlayer());
		if (pc == null && RealRP.settings().ccEnableCharacterCreator) {
			plugin.startCharacterCreator(event.getPlayer());
			event.setJoinMessage(null);
		} else if (pc != null) {
			pc.setUpNames();
		}
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		if (RealRP.settings().ccEnableCharacterCreator && plugin.isCreatingCharacter(event.getPlayer())) {
			CharacterCreator cc = plugin.getCharacterCreator(event.getPlayer());
			cc.onChat(event.getMessage());
			event.setCancelled(true);
		} else {
			plugin.onChat(event);
		}
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled() || !RealRP.settings().emEnableEmotes) {
			return;
		}
		
		// get the emote
		String[] split = event.getMessage().split(" ");
		String c = split[0].substring(1);
		Emote emote = plugin.getEmote(c);
		if (emote == null) {
			return;
		}
		
		// get the target
		String target = "";
		if (split.length > 1) {
			target = split[1];
		}
		
		// do the emote
		emote.use(event.getPlayer(), target);
		event.setCancelled(true);
	}
	
}
