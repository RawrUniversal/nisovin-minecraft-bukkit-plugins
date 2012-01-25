package com.nisovin.bookworm;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

public class BookWormWorldListener implements Listener {

	private BookWorm plugin;
	
	public BookWormWorldListener(BookWorm plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onWorldSave(WorldSaveEvent event) {
		if (event.getWorld().equals(plugin.getServer().getWorlds().get(0)) && plugin.books != null) {
			for (Book book : plugin.books.values()) {
				if (!book.isSaved()) {
					book.save();
				}
			}
		}
	}
	
}
