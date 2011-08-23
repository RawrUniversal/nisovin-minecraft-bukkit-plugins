package com.nisovin.bookworm;

import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;
import org.getspout.spoutapi.player.SpoutPlayer;

public class BookWormSpoutInventoryListener extends InventoryListener {

	public BookWormSpoutInventoryListener(BookWorm plugin) {
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
					String title = book.getTitle();
					if (title.length() > 26) title = title.substring(0, 26);
					String byLine = BookWorm.S_READ_BY + " " + book.getAuthor();
					if (byLine.length() > 26) byLine = byLine.substring(0, 26);
					player.sendNotification(title, byLine, Material.BOOK);
					event.setCancelled(true);
				}
			}
			if (itemHolding != null && itemHolding.getType() == Material.BOOK && itemClicked.getDurability() != itemHolding.getDurability()) {
				event.setCancelled(true);
			}
		}
	}
	
}
