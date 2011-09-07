package com.nisovin.oldgods.godhandlers;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

public class LoveHandler {

	public static void onEntityTarget(EntityTargetEvent event) {
		if (event.getTarget() instanceof Player && event.getReason() == TargetReason.CLOSEST_PLAYER) {
			event.setCancelled(true);
		}
	}
	
}
