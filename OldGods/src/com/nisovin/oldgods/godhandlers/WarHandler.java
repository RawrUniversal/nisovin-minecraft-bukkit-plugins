package com.nisovin.oldgods.godhandlers;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.God;
import com.nisovin.oldgods.OldGods;

public class WarHandler {

	private static final HashSet<Player> enraged = new HashSet<Player>();
	
	public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			final Player player = (Player)event.getDamager();
			String itemName = player.getItemInHand().getType().name();
			boolean rage = enraged.contains(player);
			if (itemName.contains("SWORD") || itemName.contains("AXE")) {
				event.setDamage(event.getDamage() * (rage?4:2));
			}
			if (!rage && OldGods.isDisciple(player, God.WAR) && OldGods.random() == 5) {
				enraged.add(player);
				player.sendMessage(OldGods.getDevoutMessage(God.WAR));
				Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(OldGods.plugin, new Runnable() {
					public void run() {
						enraged.remove(player);
						player.sendMessage("You are no longer enraged.");
					}
				}, 600);
			}
		} else if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
			event.setDamage(event.getDamage() / 2);
		}
	}
	
	public static void pray(Player player, Block block, int amount) {
		int chance = player.hasPermission("oldgods.disciple.war") ? 35 : 3;
		if (OldGods.random() > chance) return;
		
		Material type = null;
		int r = OldGods.random(5);
		if (r==0) {
			type = Material.IRON_CHESTPLATE;
		} else if (r==1) {
			type = Material.IRON_LEGGINGS;
		} else if (r==2) {
			type = Material.IRON_HELMET;
		} else if (r==3) {
			type = Material.IRON_BOOTS;
		} else if (r==4) {
			type = Material.DIAMOND_SWORD;
		}
		
		if (type != null) {
			Block b = block.getRelative(BlockFace.UP);
			b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(type,1));
		}		
	}
	
}
