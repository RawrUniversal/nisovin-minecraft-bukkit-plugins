package com.nisovin.bookworm.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.nisovin.bookworm.Book;

public class BookModifyEvent extends BookEvent {

    private static final HandlerList handlers = new HandlerList();
    
    private Player player;
    private ModifyType modifyType;
    private String content;
    
	public BookModifyEvent(Player player, Book book, ModifyType modifyType, String content) {
		super(book);
		
		this.player = player;
		this.modifyType = modifyType;
		this.content = content;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public ModifyType getModifyType() {
		return modifyType;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public enum ModifyType {
		NEW_TEXT_WRITTEN,
		TITLE_CHANGE,
		DISPLAY_AUTHOR_CHANGE
	}

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
