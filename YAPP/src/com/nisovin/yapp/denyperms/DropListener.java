package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DropListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onDrop(PlayerDropItemEvent event) {
		Player player = event.getPlayer();
		if (!player.isOp() && (player.hasPermission("yapp.deny.drop.*") || player.hasPermission("yapp.deny.drop." + event.getItemDrop().getItemStack().getTypeId()))) {
			event.setCancelled(true);
		}
	}
	
}
