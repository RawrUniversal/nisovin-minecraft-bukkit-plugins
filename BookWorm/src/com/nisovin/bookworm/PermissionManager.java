package com.nisovin.bookworm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PermissionManager {
	
	private PermissionHandler perm;
	
	protected PermissionManager() {
		Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");

		if (perm == null) {
			if (permissionsPlugin != null) {
				perm = ((Permissions) permissionsPlugin).getHandler();
			} else {
			}
		}		
	}
	
	public boolean canCreateBook(Player player) {
		if (perm != null && !perm.has(player, "bookworm.create")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean canModifyBook(Player player, Book book) {
		if (perm != null && !perm.has(player, "bookworm.write.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (player.isOp()) {
			return true;
		} else if (perm != null && perm.has(player, "bookworm.write.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canCopyBook(Player player, Book book) {
		if (perm != null && !perm.has(player, "bookworm.copy.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (perm != null && !perm.has(player, "bookworm.copy.others")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean canRemoveBook(Player player, Book book) {
		if (perm != null && !perm.has(player, "bookworm.remove.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (player.isOp()) {
			return true;
		} else if (perm != null && perm.has(player, "bookworm.remove.others")) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canDestroyBook(Player player, Book book) {
		if (perm != null && !perm.has(player, "bookworm.destroy.own")) {
			return false;
		} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
			return true;
		} else if (player.isOp()) {
			return true;
		} else if (perm != null && perm.has(player, "bookworm.destroy.others")) {
			return true;
		} else {
			return false;
		}		
	}
}
