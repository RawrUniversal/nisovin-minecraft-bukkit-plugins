package com.nisovin.bookworm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

public class BookWormBlockListener extends BlockListener {

	private BookWorm plugin;
	
	public BookWormBlockListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.Normal, plugin);		
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled() && event.getBlock().getType() == Material.BOOKSHELF) {
			Player player = event.getPlayer();
			Location l = event.getBlock().getLocation();
			String locStr = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
			if (plugin.bookshelves.containsKey(locStr)) {
				// get book
				short bookId = plugin.bookshelves.get(locStr);
				Book book = plugin.getBookById(bookId);
				if (book == null) {
					return;
				}
				if (plugin.perms.canDestroyBook(player, book)) {
					// remove book from bookshelf list
					plugin.bookshelves.remove(locStr);
					plugin.saveAll();
					// drop book
					l.getWorld().dropItemNaturally(l, new ItemStack(Material.BOOK, 1, book.getId()));
				} else {
					// someone else's book
					player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_CANNOT_DESTROY);
					event.setCancelled(true);
				}
			}
		}
	}	
}
