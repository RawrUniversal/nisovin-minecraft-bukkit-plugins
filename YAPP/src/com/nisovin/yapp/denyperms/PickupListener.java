package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PickupListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPickup(PlayerPickupItemEvent event) {
		Player player = event.getPlayer();
		if (!player.isOp() && (player.hasPermission("yapp.deny.pickup.*") || player.hasPermission("yapp.deny.pickup." + event.getItem().getItemStack().getTypeId()))) {
			event.setCancelled(true);
		}
	}
	
}
