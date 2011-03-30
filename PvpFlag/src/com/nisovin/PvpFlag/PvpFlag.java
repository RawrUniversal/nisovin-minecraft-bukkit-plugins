package com.nisovin.PvpFlag;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PvpFlag extends JavaPlugin {

	public HashSet<String> flagged;

	@Override
	public void onEnable() {
		flagged = new HashSet<String>();
		
		PvpCheckListener listener = new PvpCheckListener(this);
		getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, listener, Event.Priority.Normal, this);
		

	}
	
	public void onPlayerCommandPreprocess(PlayerChatEvent event) {
		if (event.getMessage().equals("checkpvp")) {
			event.setMessage("yes");
			event.setCancelled(true);
		}
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
