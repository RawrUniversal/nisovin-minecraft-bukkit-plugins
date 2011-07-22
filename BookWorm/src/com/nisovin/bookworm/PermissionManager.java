package com.nisovin.bookworm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class PermissionManager {
	
	private PermissionHandler perm = null;
	
	protected PermissionManager() {
		if (BookWorm.USE_PERMISSIONS_PLUGIN) {
			Plugin permissionsPlugin = Bukkit.getServer().getPluginManager().getPlugin("Permissions");
	
			if (perm == null && permissionsPlugin != null) {
				perm = ((Permissions) permissionsPlugin).getHandler();
			}
		}
	}
	
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
		if (perm == null) {
			return player.hasPermission(permission);
		} else {
			return perm.has(player, permission);
		}
	}
}
