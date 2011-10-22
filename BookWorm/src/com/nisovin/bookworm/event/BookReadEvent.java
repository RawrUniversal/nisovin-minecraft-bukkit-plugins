package com.nisovin.bookworm.event;

import org.bukkit.entity.Player;

import com.nisovin.bookworm.Book;

public class BookReadEvent extends BookEvent {
	private static final long serialVersionUID = 1L;
	private Player player;
	private int page;
	
	public BookReadEvent(Book book, Player player, int page) {
		super("BOOK_WORM_READ", book);
		this.player = player;
		this.page = page;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getPage() {
		return page;
	}
}
