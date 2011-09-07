package com.nisovin.oldgods.godhandlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.God;
import com.nisovin.oldgods.OldGods;

public class MiningHandler {
	
	public static void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.LAVA) {
			event.setDamage(1);
		}
	}
	
	public static void onBlockBreak(BlockBreakEvent event) {
		Material inHand = event.getPlayer().getItemInHand().getType();
		if (inHand == Material.IRON_PICKAXE || inHand == Material.GOLD_PICKAXE || inHand == Material.DIAMOND_PICKAXE) {
			Block b = event.getBlock();
			boolean devoutBlessing = (event.getPlayer().hasPermission("oldgods.disciple.mining") && OldGods.random() < 10); 
			if (b.getType() == Material.DIAMOND_ORE) {
				event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.DIAMOND, devoutBlessing ? 5 : 1));
			} else if (b.getType() == Material.IRON_ORE) {
				event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.IRON_INGOT, devoutBlessing ? 8 : 1));
			} else if (b.getType() == Material.GOLD_ORE) {
				event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.GOLD_INGOT, devoutBlessing? 6 : 1));
			} else if (b.getType() == Material.LAPIS_ORE) {
				event.getBlock().getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.INK_SACK, devoutBlessing? 8 : 2, (short)4));
			}
			if (devoutBlessing) {
				event.getPlayer().sendMessage(OldGods.getDevoutMessage(God.MINING));
			}
		}
	}
}
