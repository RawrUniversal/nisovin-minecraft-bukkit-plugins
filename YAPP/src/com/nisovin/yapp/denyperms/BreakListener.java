package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BreakListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if (!player.isOp() && (player.hasPermission("yapp.deny.break.*") || player.hasPermission("yapp.deny.break." + event.getBlock().getTypeId()))) {
			event.setCancelled(true);
		}
	}
	
}
