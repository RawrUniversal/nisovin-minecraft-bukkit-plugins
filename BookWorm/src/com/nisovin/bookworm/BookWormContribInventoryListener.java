package com.nisovin.bookworm;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;
import org.getspout.spoutapi.player.SpoutPlayer;

public class BookWormContribInventoryListener extends InventoryListener {

	public BookWormContribInventoryListener(BookWorm plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack itemClicked = event.getItem();
		ItemStack itemHolding = event.getCursor();
		if (itemClicked != null && itemClicked.getType() == Material.BOOK) {
			if (BookWorm.BOOK_INFO_ACHIEVEMENT && itemHolding == null && !event.isLeftClick() && itemClicked.getDurability() != 0) {
				Book book = BookWorm.getBook(itemClicked);
				if (book != null) {
					SpoutPlayer player = (SpoutPlayer)event.getPlayer();
					player.sendNotification(book.getTitle(), "by " + book.getAuthor(), Material.BOOK);
					event.setCancelled(true);
				}
			}
			if (itemHolding != null && itemHolding.getType() == Material.BOOK && itemClicked.getDurability() != itemHolding.getDurability()) {
				event.setCancelled(true);
			}
		}
	}
	
}
