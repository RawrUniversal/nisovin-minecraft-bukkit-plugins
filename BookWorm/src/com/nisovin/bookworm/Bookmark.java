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
		book.read(player, page);
	}
	
}
