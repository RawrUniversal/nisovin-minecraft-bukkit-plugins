package com.nisovin.bookworm;

import org.bukkit.entity.Player;

public class PermissionManager {
	
	public boolean canCreateBook(Player player) {
		return hasPerm(player, "bookworm.create");
	}
	
	public boolean canModifyBook(Player player, Book book) {
		if (!hasPerm(player, "bookworm.write.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.write.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canCopyBook(Player player, Book book) {
		if (!hasPerm(player, "bookworm.copy.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.copy.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canRemoveBook(Player player, Book book) {
		if (!hasPerm(player, "bookworm.remove.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.remove.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canDestroyBook(Player player, Book book) {
		if (!hasPerm(player, "bookworm.destroy.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.destroy.others")) {
			return true;
		} else {
			return false;
		}		
	}
	
	private boolean hasPerm(Player player, String permission) {
		return player.hasPermission(permission);
	}
}
