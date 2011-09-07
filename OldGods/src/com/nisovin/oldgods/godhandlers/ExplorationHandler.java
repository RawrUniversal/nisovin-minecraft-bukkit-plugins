package com.nisovin.oldgods.godhandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;

public class ExplorationHandler {

	public static void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL) {
			event.setDamage(event.getDamage() / 2);
		}		
	}
	
	public static void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (p.isSneaking() && event.getFrom().getY() == event.getTo().getY() && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ())) {
			p.setVelocity(p.getLocation().getDirection().setY(0).normalize().multiply(1.7));
		}
	}
	
}
