package com.nisovin.bookworm;

import org.bukkit.event.Event;
import org.bukkit.event.world.WorldListener;
import org.bukkit.event.world.WorldSaveEvent;

public class BookWormWorldListener extends WorldListener {

	private BookWorm plugin;
	
	public BookWormWorldListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.WORLD_SAVE, this, Event.Priority.Monitor, plugin);
	}
	
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
