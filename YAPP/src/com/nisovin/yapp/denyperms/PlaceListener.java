package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		if (!player.isOp() && (player.hasPermission("yapp.deny.place.*") || player.hasPermission("yapp.deny.place." + event.getBlock().getTypeId()))) {
			event.setCancelled(true);
		}
	}
	
}
