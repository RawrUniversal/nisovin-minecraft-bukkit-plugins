package com.nisovin.oldgods.godhandlers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.nisovin.oldgods.God;
import com.nisovin.oldgods.OldGods;

public class HealingHandler {

	public static void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			int damage = event.getDamage() / 2;
			event.setDamage(damage);
			Player p = (Player)event.getEntity();
			if (damage > p.getHealth() && p.hasPermission("oldgods.disciple.healing") && OldGods.random() < 50) {
				event.setDamage(0);
				p.setHealth(20);
				p.sendMessage(OldGods.getDevoutMessage(God.HEALING));
			}
		}
	}
	
	public static void pray(Player player, Location location, int amount) {
		OldGods.removeMobEffect(player, 17);
		OldGods.removeMobEffect(player, 19);
		
		player.setHealth(20);
		player.setFoodLevel(20);
	}
	
}
