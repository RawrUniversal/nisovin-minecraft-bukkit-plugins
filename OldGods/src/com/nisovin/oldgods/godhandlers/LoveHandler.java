package com.nisovin.oldgods.godhandlers;

import org.bukkit.Location;
import org.bukkit.Material;
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
	
	public static void pray(Player player, Location location, int amount) {
		int chance = player.hasPermission("oldgods.disciple.love") ? 40 : 4;
		if (OldGods.random() > chance) return;
		
		for (int i = 0; i < 3; i++) {
			location.getWorld().dropItemNaturally(location, new ItemStack(Material.RED_ROSE,1));
			location.getWorld().dropItemNaturally(location, new ItemStack(Material.YELLOW_FLOWER,1));
		}
	}
	
}
