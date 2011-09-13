package com.nisovin.bookworm;

import org.bukkit.entity.Player;

public class Bookmark {
	
	protected Book book;
	protected int page;
	protected long lastRead;
	
	public Bookmark() {
	}
	
	public void readBook(Player player, Book book) {
		if (book == null || book != this.book) {
			this.book = book;
			this.page = 0;
		} else {
			this.page++;
		}
		lastRead = System.currentTimeMillis();
		read(player);
	}
	
	public void nextPage(Player player) {
		this.page++;
		read(player);
	}
	
	public void previousPage(Player player) {
		this.page--;
		if (this.page < 0) {
			this.page = 0;
		}
		read(player);
	}
	
	private void read(Player player) {
		//if (BookWorm.SPOUT_ENABLED) {
		//	boolean shown = SpoutHandle.showBook(player, book, page);
		//	if (!shown) {
		//		book.read(player, page);
		//	}
		//} else {
			book.read(player, page);
		//}
	}
	
}
