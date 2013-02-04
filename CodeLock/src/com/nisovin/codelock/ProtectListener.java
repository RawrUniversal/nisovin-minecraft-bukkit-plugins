package com.nisovin.codelock;

import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ProtectListener implements Listener {
	
	CodeLock plugin;
	
	BlockFace[] directions = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	
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
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		if (block.getType() != Material.CHEST) return;
		
		for (BlockFace face : directions) {
			Block b = block.getRelative(face);
			if (b.getType() == Material.CHEST && plugin.isLocked(b)) {
				event.setCancelled(true);
				return;
			}
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
