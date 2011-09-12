package com.nisovin.oldgods.godhandlers;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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
	
}
