package com.nisovin.bookworm;

public class BookUnloader implements Runnable {

	private static final int CLEAN_INTERVAL = 600;
	private static final int REMOVE_DELAY = 300;

	private BookWorm plugin;
	
	public BookUnloader(BookWorm plugin) {
		this.plugin = plugin;
		
		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, this, CLEAN_INTERVAL, CLEAN_INTERVAL);
	}
	
	public void run() {
		// clean new books
		for (String name : plugin.newBooks.keySet().clone()) {
			Player p = plugin.getServer().getPlayer(name);
			if (p == null || !p.isOnline()) {
				plugin.newBooks.remove(name);
			}
		}
		
		// clean bookmarks
		for (String name : plugins.bookmarks.keySet().clone()) {
			Player p = plugin.getServer().getPlayer(name);
			if (p == null || !p.isOnline() || plugins.bookmarks.get(name).lastRead + REMOVE_DELAY*1000 < System.currentTimeMillis()) {
				plugins.bookmarks.remove(name);
			}
		}
		
		// unload unused books
		for (Book book : plugin.books.values()) {
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