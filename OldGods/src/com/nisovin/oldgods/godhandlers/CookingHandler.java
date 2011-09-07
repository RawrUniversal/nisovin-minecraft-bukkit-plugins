package com.nisovin.oldgods.godhandlers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.inventory.ItemStack;

public class CookingHandler {

	public static void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Creature) {
			List<ItemStack> drops = event.getDrops();
			for (ItemStack i : drops) {
				if (i.getType() == Material.PORK) {
					i.setType(Material.GRILLED_PORK);
				}
			}
		}
	}
	
	public static void onFurnaceBurn(FurnaceBurnEvent event) {
		event.setBurnTime(event.getBurnTime() * 3);
	}
	
}
