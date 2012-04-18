package com.nisovin.bookworm;

import java.util.Iterator;

import org.bukkit.entity.Player;

class BookUnloader implements Runnable {

	private BookWorm plugin;
	
	public BookUnloader(BookWorm plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, BookWorm.CLEAN_INTERVAL*20, BookWorm.CLEAN_INTERVAL*20);
	}
	
	public void run() {		
		// clean bookmarks
		if (BookWorm.REMOVE_DELAY > 0) {
			Iterator<String> i = plugin.bookmarks.keySet().iterator();
			while (i.hasNext()) {
				String name = i.next();
				Player p = plugin.getServer().getPlayer(name);
				if (p == null || !p.isOnline() || plugin.bookmarks.get(name).lastRead + BookWorm.REMOVE_DELAY*1000 < System.currentTimeMillis()) {
					i.remove();
				}
			}
		}
		
		// unload unused books
		for (Book book : plugin.books.values()) {
			if (!book.isSaved()) {
				book.save();
			}
			if (book.isLoaded()) {
				boolean found = false;
				for (Bookmark bookmark : plugin.bookmarks.values()) {
					if (bookmark.book == book) {
						found = true;
						break;
					}
				}
				if (!found) {
					book.unload();
				}
			}
		}
	}
	
	public void stop() {
		plugin.getServer().getScheduler().cancelTasks(plugin);
	}

}