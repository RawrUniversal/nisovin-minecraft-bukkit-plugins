package com.nisovin.bookworm;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkitcontrib.event.inventory.InventoryClickEvent;
import org.bukkitcontrib.event.inventory.InventoryListener;

public class BookWormContribInventoryListener extends InventoryListener {

	public BookWormContribInventoryListener(BookWorm plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack itemClicked = event.getItem();
		ItemStack itemHolding = event.getCursor();
		if (itemClicked != null && itemClicked.getType() == Material.BOOK && itemHolding != null && itemHolding.getType() == Material.BOOK) {
			if (itemClicked.getDurability() != itemHolding.getDurability()) {
				event.setCancelled(true);
			}
		}
	}
	
}
