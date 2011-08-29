package com.nisovin.IronGates;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class IGBlockListener extends BlockListener {

	IronGates plugin;
	
	public IGBlockListener(IronGates plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled() && event.getBlock().getType() == Material.IRON_BLOCK && !event.getPlayer().isOp()) {
			Block b = event.getBlock();
			int bx = b.getX();
			int by = b.getY();
			int bz = b.getZ();
			for (Gate gate : plugin.gates.values()) {
				Location loc = gate.getEntrance();
				if (Math.abs(loc.getBlockX()-bx) <= 1 && Math.abs(loc.getBlockZ()-bz) <= 1 && Math.abs(loc.getBlockY()-by) <= 3) {
					event.setCancelled(true);
				}
			}
		}
	}

}
