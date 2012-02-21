package com.nisovin.bookworm;

import org.bukkit.entity.Player;

public class PermissionManager {
	
	public boolean canCreateBook(Player player) {
		return hasPerm(player, "bookworm.create");
	}
	
	public boolean canModifyBook(Player player, Book book) {
		if (BookWorm.USE_DENY_PERMS && hasPerm(player, "bookworm.write.deny." + book.getId())) {
			return false;
		} else if (!hasPerm(player, "bookworm.write.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.write.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canChangeBookAuthor(Player player, Book book) {
		if (!hasPerm(player, "bookworm.setauthor.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.setauthor.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canCopyBook(Player player, Book book) {
		if (BookWorm.USE_DENY_PERMS && hasPerm(player, "bookworm.copy.deny." + book.getId())) {
			return false;
		} else if (!hasPerm(player, "bookworm.copy.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.copy.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canPlaceBook(Player player, Book book) {
		if (BookWorm.USE_DENY_PERMS && hasPerm(player, "bookworm.place.deny." + book.getId())) {
			return false;
		} else if (!hasPerm(player, "bookworm.place.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.place.others")) {
			return true;
		} else {
			return false;
		}		
	}
	
	public boolean canRemoveBook(Player player, Book book) {
		if (BookWorm.USE_DENY_PERMS && hasPerm(player, "bookworm.remove.deny." + book.getId())) {
			return false;
		} else if (!hasPerm(player, "bookworm.remove.own")) {
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
		if (BookWorm.USE_DENY_PERMS && hasPerm(player, "bookworm.destroy.deny." + book.getId())) {
			return false;
		} else if (!hasPerm(player, "bookworm.destroy.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.destroy.others")) {
			return true;
		} else {
			return false;
		}		
	}
	
	public boolean canSpawnBook(Player player, Book book) {
		if (BookWorm.USE_DENY_PERMS && hasPerm(player, "bookworm.get.deny." + book.getId())) {
			return false;
		} else if (!hasPerm(player, "bookworm.get.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (hasPerm(player, "bookworm.get.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean hasPerm(Player player, String permission) {
		return player.hasPermission(permission);
	}
}
