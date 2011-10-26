package com.nisovin.oldgods.godhandlers;

import org.bukkit.Location;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class DeathHandler {

	public static void onEntityCombust(EntityCombustEvent event) {
		event.setCancelled(true);
	}
	
	public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player && event.getDamager() instanceof Monster) {
			event.setDamage(event.getDamage() * 2);
		}
	}
	
	public static void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() == SpawnReason.NATURAL) {
			CreatureType c = event.getCreatureType();
			if (c == CreatureType.CREEPER || c == CreatureType.PIG_ZOMBIE || c == CreatureType.SKELETON || c == CreatureType.SPIDER || c == CreatureType.ZOMBIE) {
				event.getEntity().getWorld().spawnCreature(event.getEntity().getLocation(), c);
			}
		}		
	}
	
	public static void pray(Player player, Location location, int amount) {
		
	}
}
