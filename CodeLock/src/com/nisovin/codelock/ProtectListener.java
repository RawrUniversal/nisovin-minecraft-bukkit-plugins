package com.nisovin.codelock;

import java.util.Iterator;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ProtectListener implements Listener {
	
	CodeLock plugin;
	
	public ProtectListener(CodeLock plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().hasPermission("codelock.bypass") && plugin.isLocked(event.getBlock())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onExplode(EntityExplodeEvent event) {
		List<Block> blocks = event.blockList();
		Iterator<Block> iter = blocks.iterator();
		while (iter.hasNext()) {
			Block block = iter.next();
			if (plugin.isLocked(block)) {
				iter.remove();
			}
		}
	}

}
