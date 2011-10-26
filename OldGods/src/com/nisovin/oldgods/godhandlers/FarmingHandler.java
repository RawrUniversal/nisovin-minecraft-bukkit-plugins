package com.nisovin.oldgods.godhandlers;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
	
	public static void pray(Player player, Location location, int amount) {
		int chance = player.hasPermission("oldgods.disciple.farming") ? 40 : 4;
		if (OldGods.random() > chance) return;
		
		int quantity = 0;
		Material type = null;
		int r = OldGods.random(5);
		if (r==0) {
			type = Material.IRON_HOE;
			quantity = 1;
		} else if (r==1) {
			type = Material.WHEAT;
			quantity = 10;
		} else if (r==2) {
			type = Material.MELON;
			quantity = 4;
		} else if (r==3) {
			type = Material.SEEDS;
			quantity = 10;
		} else if (r==4) {
			type = Material.MELON_SEEDS;
			quantity = 10;
		}
		
		if (quantity > 0 && type != null) {
			for (int i = 0; i < quantity; i++) {
				location.getWorld().dropItemNaturally(location, new ItemStack(type,1));
			}
		}		
	}

}
