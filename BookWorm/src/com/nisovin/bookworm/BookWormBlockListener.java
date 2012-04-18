package com.nisovin.bookworm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

class BookWormBlockListener implements Listener {

	private BookWorm plugin;
	
	public BookWormBlockListener(BookWorm plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled() && event.getBlock().getType() == Material.BOOKSHELF) {
			Player player = event.getPlayer();
			Location l = event.getBlock().getLocation();
			String locStr = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
			boolean dropShelf = BookWorm.DROP_BOOKSHELF;
			if (plugin.bookshelves.containsKey(locStr)) {
				// get book
				short bookId = plugin.bookshelves.get(locStr);
				Book book = plugin.getBookById(bookId);
				if (book == null) {
					// do nothing
				} else if (plugin.perms.canDestroyBook(player, book)) {
					// remove book from bookshelf list
					plugin.bookshelves.remove(locStr);
					plugin.saveBookshelves();
					// drop book
					l.getWorld().dropItemNaturally(l, new ItemStack(Material.BOOK, 1, book.getId()));
				} else {
					// someone else's book
					player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_CANNOT_DESTROY);
					event.setCancelled(true);
					dropShelf = false;
				}
			}
			if (dropShelf) {
				l.getWorld().dropItemNaturally(l, new ItemStack(Material.BOOKSHELF, 1));
			}
		}
	}	
}
