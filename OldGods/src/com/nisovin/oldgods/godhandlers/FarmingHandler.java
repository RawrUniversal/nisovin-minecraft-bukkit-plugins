package com.nisovin.oldgods.godhandlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class FarmingHandler {
	
	public static void onBlockBreak(BlockBreakEvent event) {
		Material inHand = event.getPlayer().getItemInHand().getType();
		if (inHand == Material.IRON_HOE || inHand == Material.GOLD_HOE || inHand == Material.DIAMOND_HOE) {
			Block b = event.getBlock();
			if (b.getType() == Material.CROPS) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.WHEAT, 1));
			}
		}
	}

}
