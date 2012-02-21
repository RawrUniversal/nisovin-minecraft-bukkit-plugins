package com.nisovin.bookworm.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.nisovin.bookworm.Book;

public class BookReadEvent extends BookEvent {

    private static final HandlerList handlers = new HandlerList();
    
	private Player player;
	private int page;
	
	public BookReadEvent(Book book, Player player, int page) {
		super(book);
		this.player = player;
		this.page = page;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getPage() {
		return page;
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
	
}
