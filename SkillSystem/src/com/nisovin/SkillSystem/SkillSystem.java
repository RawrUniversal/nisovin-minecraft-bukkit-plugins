package com.nisovin.SkillSystem;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;

public class SkillSystem extends JavaPlugin {

	public static Server server;

	@Override
	public void onEnable() {
		
		server = getServer();
		
		SSBlockListener blockListener = new SSBlockListener(this);
		
		server.getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		server.getPluginManager().registerEvent(Event.Type.BLOCK_PLACED, blockListener, Priority.Normal, this);
		
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
