package com.nisovin.oldgods.godhandlers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.God;
import com.nisovin.oldgods.OldGods;

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
	
	public static void onFurnaceSmelt(FurnaceSmeltEvent event) {
		if (OldGods.random() == 1) {
			Material item = event.getResult().getType();
			if (item == Material.COOKED_FISH || item == Material.GRILLED_PORK || item == Material.BREAD) {
				Block furnace = event.getFurnace();
				for (Player p : furnace.getWorld().getPlayers()) {
					if (OldGods.isDisciple(p, God.COOKING) && p.getLocation().distanceSquared(furnace.getLocation()) < 5*5) {
						for (int i = 0; i < 10; i++) {
							furnace.getWorld().dropItemNaturally(furnace.getLocation(), new ItemStack(item, 1));
						}
						p.sendMessage(OldGods.getDevoutMessage(God.COOKING));
						break;
					}
				}
			}
		}
	}
	
}
