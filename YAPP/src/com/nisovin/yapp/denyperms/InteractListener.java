package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InteractListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		if (!player.isOp() && (player.hasPermission("yapp.deny.interact.*") || player.hasPermission("yapp.deny.interact." + getEntityTypeId(event.getRightClicked())))) {
			event.setCancelled(true);
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
