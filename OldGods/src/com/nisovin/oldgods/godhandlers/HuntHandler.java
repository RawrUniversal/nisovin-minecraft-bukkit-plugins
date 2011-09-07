package com.nisovin.oldgods.godhandlers;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.God;
import com.nisovin.oldgods.OldGods;

public class HuntHandler {

	public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Arrow && ((Arrow)event.getDamager()).getShooter() instanceof Player) {
			event.setDamage(event.getDamage() * 2);
		}
	}
	
	public static void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Creature) {
			List<ItemStack> drops = event.getDrops();
			int max = drops.size();
			for (int i = 0; i < max; i++) {
				drops.add(drops.get(i).clone());
			}
			
			if (OldGods.random() < 5) {
				Player killer = null;
				if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
					if (((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager() instanceof Player) {
						killer = (Player)((EntityDamageByEntityEvent)event.getEntity().getLastDamageCause()).getDamager();
					}
				}
				if (killer != null && killer.hasPermission("oldgods.disciple.hunt")) {
					// add special drops
					Material mat = Material.STONE;
					Entity entity = event.getEntity();
					if (entity instanceof Cow) {
						mat = Material.LEATHER;
					} else if (entity instanceof Pig) {
						mat = Material.GRILLED_PORK;
					} else if (entity instanceof Sheep) {
						mat = Material.WOOL;
					} else if (entity instanceof Chicken) {
						mat = Material.FEATHER;
					} else if (entity instanceof Squid) {
						mat = Material.INK_SACK;
					} else if (entity instanceof Spider) {
						mat = Material.STRING;
					} else if (entity instanceof Skeleton) {
						mat = Material.ARROW;
					} else if (entity instanceof Creeper) {
						mat = Material.SULPHUR;
					} else if (entity instanceof Slime) {
						mat = Material.SLIME_BALL;
					}
					ItemStack item = new ItemStack(mat, 1);
					for (int i = 0; i < 8; i++) {
						drops.add(item.clone());
					}
					killer.sendMessage(OldGods.getDevoutMessage(God.HUNT));
				}
			}
		}		
	}
	
}
