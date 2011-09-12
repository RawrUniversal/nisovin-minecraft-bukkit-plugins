package com.nisovin.oldgods.godhandlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.God;
import com.nisovin.oldgods.OldGods;

public class FarmingHandler {
	
	public static void onBlockBreak(BlockBreakEvent event) {
		Material m = null;
		Block b = event.getBlock();
		Material inHand = event.getPlayer().getItemInHand().getType();
		if (inHand == Material.IRON_HOE || inHand == Material.GOLD_HOE || inHand == Material.DIAMOND_HOE) {
			if (b.getType() == Material.CROPS) {
				m = Material.WHEAT;
			}
		}
		boolean extra = false;
		if (OldGods.isDisciple(event.getPlayer(), God.FARMING) && OldGods.random() == 10) {
			extra = true;
		}
		if (m != null) {
			b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(m, extra?10:1));
			if (extra) {
				event.getPlayer().sendMessage(OldGods.getDevoutMessage(God.FARMING));
			}
		}
	}

}
