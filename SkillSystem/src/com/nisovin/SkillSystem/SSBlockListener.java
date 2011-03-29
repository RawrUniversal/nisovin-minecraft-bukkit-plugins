package com.nisovin.SkillSystem;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class SSBlockListener extends BlockListener {

	SkillSystem plugin;
	
	public SSBlockListener(SkillSystem plugin) {
		this.plugin = plugin;
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled()) {
			Block block = event.getBlock();
			if (block.getType() == Material.STONE) {
				event.getPlayer().getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.COBBLESTONE));
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!event.isCancelled()) {
			Block block = event.getBlockPlaced();
			if (block.getType() == Material.DIRT) {
				//block.setType(Material.GRASS);
				Block b = event.getPlayer().getLocation().getWorld().getBlockAt(block.getLocation());
				b.setType(Material.GRASS);
			}
		}
	}
}
