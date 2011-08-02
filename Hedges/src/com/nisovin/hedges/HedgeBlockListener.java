package com.nisovin.hedges;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;

public class HedgeBlockListener extends BlockListener {

	Hedges plugin;
	
	public HedgeBlockListener(Hedges plugin) {
		this.plugin = plugin;
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
