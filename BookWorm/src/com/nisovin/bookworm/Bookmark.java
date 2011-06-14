package com.nisovin.bookworm;

import org.bukkit.entity.Player;

public class Bookmark {
	
	protected Book book;
	protected int page;
	
	public Bookmark() {
	}
	
	public void readBook(Player player, Book book) {
		if (book == null || book != this.book) {
			this.book = book;
			this.page = 0;
		} else {
			this.page++;
		}
		book.read(player, page);
	}
	
}
