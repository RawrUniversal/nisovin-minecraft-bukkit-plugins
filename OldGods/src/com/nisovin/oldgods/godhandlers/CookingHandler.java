package com.nisovin.oldgods.godhandlers;

import java.util.List;

import org.bukkit.Location;
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
	
	public static void pray(Player player, Location location, int amount) {
		int chance = player.hasPermission("oldgods.disciple.cooking") ? 40 : 4;
		if (OldGods.random() > chance) return;
		
		int quantity = 0;
		Material type = null;
		int r = OldGods.random(3);
		if (r==0) {
			type = Material.BREAD;
			quantity = 8;
		} else if (r==1) {
			type = Material.GRILLED_PORK;
			quantity = 4;
		} else if (r==2) {
			type = Material.CAKE;
			quantity = 1;
		}
		
		if (quantity > 0 && type != null) {
			for (int i = 0; i < quantity; i++) {
				location.getWorld().dropItemNaturally(location, new ItemStack(type,1));
			}
		}
	}
	
}
