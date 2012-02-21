package com.nisovin.bookworm.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.nisovin.bookworm.Book;

public abstract class BookEvent extends Event implements Cancellable {

	private Book book;
	private boolean cancelled;
	
	protected BookEvent(Book book) {
		this.book = book;
	}
	
	public Book getBook() {
		return book;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	// TODO: onBookCreate, onBookCopy, onBookRemove, onBookDestroy, onBookDelete

}
