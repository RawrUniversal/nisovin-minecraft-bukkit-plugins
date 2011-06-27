package com.nisovin.bookworm.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.bookworm.Book;

public class BookPlaceEvent extends BookEvent {

	private static final long serialVersionUID = 1L;
	private Player player;
	private Location location;
	
	public BookPlaceEvent(String name, Player player, Book book, Location location) {
		super(name, book);
		
		this.player = player;
		this.location = location;
	}

	public Player getPlayer() {
		return player;
	}
	
	public Location getLocation() {
		return location;
	}

}
