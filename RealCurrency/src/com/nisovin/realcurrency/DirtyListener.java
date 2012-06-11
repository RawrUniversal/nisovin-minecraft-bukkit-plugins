package com.nisovin.realcurrency;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class DirtyListener implements Listener {

	RealCurrency plugin;
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onInventoryClick(InventoryClickEvent event) {
		Wallet wallet = plugin.getWallet((Player)event.getWhoClicked());
		if (wallet != null) {
			wallet.setDirty();
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onItemDrop(PlayerDropItemEvent event) {
		Wallet wallet = plugin.getWallet((Player)event.getPlayer());
		if (wallet != null) {
			wallet.setDirty();
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onItemPickup(PlayerPickupItemEvent event) {
		Wallet wallet = plugin.getWallet((Player)event.getPlayer());
		if (wallet != null) {
			wallet.setDirty();
		}
	}
	
}
