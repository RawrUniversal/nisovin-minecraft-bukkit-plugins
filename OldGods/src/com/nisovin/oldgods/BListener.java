package com.nisovin.oldgods;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.plugin.PluginManager;

import com.nisovin.oldgods.godhandlers.*;

public class BListener extends BlockListener {

	private OldGods plugin;
	
	public BListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Monitor, plugin);
	}

	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.MINING) {
			MiningHandler.onBlockBreak(event);
		} else if (god == God.FARMING) {
			FarmingHandler.onBlockBreak(event);
		}
	}
	
	
	
}
