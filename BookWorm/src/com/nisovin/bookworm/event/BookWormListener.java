package com.nisovin.bookworm.event;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class BookWormListener extends CustomEventListener {
	
	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof BookReadEvent) {
			onBookRead((BookReadEvent)event);
		} else if (event instanceof BookPlaceEvent) {
			onBookPlace((BookPlaceEvent)event);
		}
	}
	
	public void onBookRead(BookReadEvent event) {		
	}
	
	public void onBookPlace(BookPlaceEvent event) {		
	}
	
	// TODO: onBookCreate, onBookCopy, onBookRemove, onBookDestroy, onBookDelete
	
}
