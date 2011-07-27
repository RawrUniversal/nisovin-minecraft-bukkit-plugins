package com.nisovin.hedges;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.plugin.PluginManager;

public class HedgeBlockListener extends BlockListener {

	Hedges plugin;
	
	public HedgeBlockListener(Hedges plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.LEAVES_DECAY, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onLeavesDecay(LeavesDecayEvent event) {
		Block block = event.getBlock();
		if ((block.getData() & 4) == 4) { // leaf was built, don't decay
			event.setCancelled(true); // cancelled decay
			block.setData((byte) (block.getData() & 7)); // clear check-decay bit
		}
	}
	
	@Override
	public void onBlockPlace(final BlockPlaceEvent event) {
		if (!event.isCancelled() && event.getBlock().getType() == Material.LEAVES) {
			// run next tick so it is already placed
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Block block = event.getBlock();
					block.setData((byte) (block.getData() | 4)); // set built flag
				}
			}, 1);
		}
	}
	
}
