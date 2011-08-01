package com.nisovin.realrp.listeners;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.PluginManager;

import com.nisovin.realrp.RealRP;
import com.nisovin.realrp.chat.Emote;

public class RPPlayerListener extends PlayerListener {

	private RealRP plugin;
	
	public RPPlayerListener(RealRP plugin) {
		this.plugin = plugin;
		
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
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
	}
	
}
