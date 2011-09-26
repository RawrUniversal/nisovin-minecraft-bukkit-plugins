package com.nisovin.oldgods.godhandlers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.OldGods;

public class LoveHandler {

	public static void onEntityTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player && event.getReason() == TargetReason.CLOSEST_PLAYER) {
			event.setCancelled(true);
		}
	}
	
	public static void pray(Player player, Block block, int amount) {
		int chance = player.hasPermission("oldgods.disciple.love") ? 40 : 4;
		if (OldGods.random() > chance) return;
		
		Block b = block.getRelative(BlockFace.UP);
		for (int i = 0; i < 3; i++) {
			b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.RED_ROSE,1));
			b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(Material.YELLOW_FLOWER,1));
		}
	}
	
}
