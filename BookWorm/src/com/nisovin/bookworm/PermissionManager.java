package com.nisovin.bookworm;

import org.bukkit.entity.Player;

public class PermissionManager {
	public boolean canModifyBook(Player player, Book book) {
		if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (player.isOp()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canCopyBook(Player player, Book book) {
		return true;
	}
	
	public boolean canRemoveBook(Player player, Book book) {
		if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (player.isOp()) {
			return true;
		} else {
			return false;
		}
	}
}
