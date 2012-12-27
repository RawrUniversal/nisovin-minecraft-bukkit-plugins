package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class TargetListener implements Listener {

	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			Player player = (Player)event.getTarget();
			if (!player.isOp() && (player.hasPermission("yapp.deny.targeted.*") || player.hasPermission("yapp.deny.targeted." + event.getEntityType().getTypeId()))) {
				event.setCancelled(true);
			}
		}
	}
	
}
