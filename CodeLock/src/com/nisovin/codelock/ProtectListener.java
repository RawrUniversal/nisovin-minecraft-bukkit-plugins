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
import org.bukkit.event.vehicle.VehicleCreateEvent;

public class ProtectListener implements Listener {
	
	CodeLock plugin;
	
	BlockFace[] chestCheckDirs = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
	BlockFace[] hopperCheckDirs = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
	
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
		Material type = block.getType();
		int id = block.getTypeId();
		if (type == Material.CHEST) {
			for (BlockFace face : chestCheckDirs) {
				Block b = block.getRelative(face);
				if (b.getType() == Material.CHEST && plugin.isLocked(b)) {
					event.setCancelled(true);
					return;
				}
			}
		} else if (id == 154 /* HOPPER */) {
			for (BlockFace face : hopperCheckDirs) {
				Block b = block.getRelative(face);
				if (b.getType() == Material.CHEST && plugin.isLocked(b)) {
					event.setCancelled(true);
					return;
				}
			}
		} else if (type == Material.RAILS || type == Material.POWERED_RAIL || type == Material.DETECTOR_RAIL || id == 157 /*ACTIVATOR_RAIL*/) {
			if (plugin.isLocked(block.getRelative(BlockFace.UP))) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onMinecartPlace(VehicleCreateEvent event) {
		if (plugin.isLocked(event.getVehicle().getLocation().getBlock().getRelative(BlockFace.UP))) {
			event.getVehicle().remove();
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
