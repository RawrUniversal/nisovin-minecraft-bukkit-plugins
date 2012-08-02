package com.nisovin.yapp.denyperms;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

public class CraftListener implements Listener {

	@EventHandler(priority=EventPriority.LOW)
	public void onPreCraft(PrepareItemCraftEvent event) {
		Player player = (Player)event.getViewers().get(0);
		if (!player.isOp() && (player.hasPermission("yapp.deny.craft.*") || player.hasPermission("yapp.deny.craft." + event.getRecipe().getResult().getTypeId()))) {
			event.getInventory().setResult(null);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onCraft(CraftItemEvent event) {
		Player player = (Player)event.getWhoClicked();
		if (!player.isOp() && (player.hasPermission("yapp.deny.craft.*") || player.hasPermission("yapp.deny.craft." + event.getCurrentItem().getTypeId()))) {
			event.setCancelled(true);
		}
	}
	
}
