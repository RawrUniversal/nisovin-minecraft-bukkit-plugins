package com.nisovin.oldgods.godhandlers;

import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class WarHandler {

	public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			String itemName = ((Player)event.getDamager()).getItemInHand().getType().name();
			if (itemName.contains("SWORD") || itemName.contains("AXE")) {
				event.setDamage(event.getDamage() * 2);					
			}
		} else if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
			event.setDamage(event.getDamage() / 2);
		}
	}
	
}
