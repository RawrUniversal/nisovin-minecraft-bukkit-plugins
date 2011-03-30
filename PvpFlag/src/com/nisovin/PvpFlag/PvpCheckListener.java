package com.nisovin.PvpFlag;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class PvpCheckListener extends CustomEventListener {
	
	PvpFlag plugin;
	
	public PvpCheckListener(PvpFlag plugin) {
		this.plugin = plugin;
	}
	
	public void onCustomEvent(Event event) {
		if (event.getEventName().equals("pvpcheck")) {
			plugin.getServer().getLogger().severe("IT WORKS!");
		}
	}
}
