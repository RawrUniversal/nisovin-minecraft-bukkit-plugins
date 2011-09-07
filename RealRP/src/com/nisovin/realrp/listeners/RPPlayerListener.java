package com.nisovin.realrp.listeners;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.getspout.spoutapi.player.SpoutPlayer;

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
		pm.registerEvent(Event.Type.PLAYER_QUIT, this, Event.Priority.Highest, plugin);
		pm.registerEvent(Event.Type.PLAYER_CHAT, this, Event.Priority.High, plugin);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerJoin(PlayerJoinEvent event) {
		String joinFormat = RealRP.replaceColorCodes(RealRP.settings().gsJoinMessageFormat);
		PlayerCharacter pc = PlayerCharacter.get(event.getPlayer(), true);
		if (pc == null && RealRP.settings().ccEnableCharacterCreator) {
			plugin.startCharacterCreator(event.getPlayer());
			if (!joinFormat.isEmpty()) {
				event.setJoinMessage(null);
			}
		} else if (pc != null) {
			pc.setUpNames();
			if (!joinFormat.isEmpty()) {
				event.setJoinMessage(pc.fillInNames(joinFormat));
			}
		}
		if (!RealRP.settings().gsEncourageSpoutMessage.isEmpty()) {
			final SpoutPlayer player = (SpoutPlayer)event.getPlayer();
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					if (!player.isSpoutCraftEnabled()) {
						RealRP.sendMessage(player, RealRP.settings().gsEncourageSpoutMessage);
					}
				}
			}, 100);
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		String quitFormat = RealRP.replaceColorCodes(RealRP.settings().gsQuitMessageFormat);
		if (!quitFormat.isEmpty()) {
			PlayerCharacter pc = PlayerCharacter.get(event.getPlayer());
			if (pc != null) {
				event.setQuitMessage(pc.fillInNames(quitFormat));
			}
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
		
		// get command
		String[] split = event.getMessage().split(" ");
		String c = split[0].substring(1);
		
		// get the emote
		if (c.equalsIgnoreCase("me") && split.length > 1 && RealRP.settings().emUseMeCommand) {
			// me command
			StringBuilder message = new StringBuilder();
			for (int i = 1; i < split.length; i++) {
				message.append(split[i] + " ");
			}
			RealRP.getPlugin().getEmoteManager().sendGenericEmote(event.getPlayer(), message.toString().trim());
			event.setCancelled(true);
			return;
		}
		
		// get emote
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
