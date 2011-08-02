package com.nisovin.hedges;

import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Hedges extends JavaPlugin {

	private HedgeBlockListener listener;
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		listener = new HedgeBlockListener(this);
		pm.registerEvent(Event.Type.LEAVES_DECAY, listener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, listener, Event.Priority.Monitor, this);
		
		getServer().getLogger().info("Hedges v" + getDescription().getVersion() + " enabled.");
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info("Hedges v" + getDescription().getVersion() + " disabled.");
	}
}
