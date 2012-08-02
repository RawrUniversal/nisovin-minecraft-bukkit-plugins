package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class UseListener implements Listener {

	boolean checkItem;
	boolean checkBlock;
	
	public UseListener(boolean checkItem, boolean checkBlock) {
		this.checkItem = checkItem;
		this.checkBlock = checkBlock;
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (!player.isOp()) {
			if (checkItem && event.hasItem()) {
				if (player.hasPermission("yapp.deny.useitem.*") || player.hasPermission("yapp.deny.useitem." + event.getItem().getTypeId())) {
					event.setCancelled(true);
				}
			}
			if (checkBlock && event.hasBlock()) {
				if (player.hasPermission("yapp.deny.useblock.*") || player.hasPermission("yapp.deny.useblock." + event.getClickedBlock().getTypeId())) {
					event.setCancelled(true);
				}
			}
		}
	}
	
}
