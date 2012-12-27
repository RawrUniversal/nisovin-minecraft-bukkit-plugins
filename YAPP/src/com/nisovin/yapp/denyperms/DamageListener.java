package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

	boolean checkAttack;
	boolean checkDamage;
	
	public DamageListener(boolean checkAttack, boolean checkDamage) {
		this.checkAttack = checkAttack;
		this.checkDamage = checkDamage;
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onDamage(EntityDamageEvent event) {
		if (checkDamage && event.getEntity().getType() == EntityType.PLAYER) {
			// player receiving general damage
			Player player = (Player)event.getEntity();
			if (!player.isOp() && (player.hasPermission("yapp.deny.damage.*") || player.hasPermission("yapp.deny.damage." + event.getCause().name().toLowerCase()))) {
				event.setCancelled(true);
				return;
			}
		}
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
			Player player = null;
			if (checkAttack) {
				if (evt.getDamager().getType() == EntityType.PLAYER) {
					player = (Player)evt.getDamager();
				} else if (evt.getDamager() instanceof Projectile) {
					Projectile proj = (Projectile)evt.getDamager();
					if (proj.getShooter().getType() == EntityType.PLAYER) {
						player = (Player)proj.getShooter();
					}
				}
				if (player != null) {
					// player attacking
					if (!player.isOp() && (player.hasPermission("yapp.deny.attack.*") || player.hasPermission("yapp.deny.attack." + getEntityTypeId(event.getEntity())))) {
						event.setCancelled(true);
						return;
					}
				}
			}
			if (checkDamage && evt.getEntity().getType() == EntityType.PLAYER) {
				// player receiving entity damage
				if (!player.isOp() && (player.hasPermission("yapp.deny.damage.*") || player.hasPermission("yapp.deny.damage." + getEntityTypeId(evt.getDamager())))) {
					event.setCancelled(true);
					return;
				}
				if (evt.getDamager() instanceof Projectile) {
					Projectile proj = (Projectile)evt.getDamager();
					if (!player.isOp() && player.hasPermission("yapp.deny.damage." + getEntityTypeId(proj.getShooter()))) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	private int getEntityTypeId(Entity entity) {
		if (entity.getType() == EntityType.PLAYER) {
			return 0;
		} else {
			return entity.getType().getTypeId();
		}
	}
	
}
