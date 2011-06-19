package com.nisovin.bookworm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class BookWormPlayerListener extends PlayerListener {
	
	private BookWorm plugin;
	
	public BookWormPlayerListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		if (BookWorm.SHOW_TITLE_ON_HELD_CHANGE) {
			plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM_HELD, this, Event.Priority.Monitor, plugin);
		}
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();
		if (!event.isCancelled() && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() == Material.BOOKSHELF) {
			Location l = event.getClickedBlock().getLocation();
			String locStr = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
			if (plugin.bookshelves.containsKey(locStr)) {
				// get book
				short bookId = plugin.bookshelves.get(locStr);
				Book book = plugin.getBook(bookId);
				
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					// right click - just set bookmark and read
					
					// get player bookmark
					Bookmark bookmark = plugin.bookmarks.get(player.getName());
					if (bookmark == null) {
						bookmark = new Bookmark();
						plugin.bookmarks.put(player.getName(), bookmark);
					}
					
					// set bookmark and read
					bookmark.readBook(player, book);					
				} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {	
					if (!player.isSneaking() && plugin.perms.canCopyBook(player, book) && ((!BookWorm.REQUIRE_BOOK_TO_COPY && inHand.getType() == Material.AIR) || (inHand != null && inHand.getType() == Material.BOOK && inHand.getDurability() == 0))) {				
						// copy book if allowed
						if (BookWorm.REQUIRE_BOOK_TO_COPY) {
							player.getItemInHand().setDurability(bookId);
						} else {
							player.setItemInHand(new ItemStack(Material.BOOK, 1, bookId));
						}
						player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COPIED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());
					} else if (player.isSneaking() && plugin.perms.canRemoveBook(player, book) && inHand.getType() == Material.AIR) {
						// remove book if allowed
						player.setItemInHand(new ItemStack(Material.BOOK, 1, bookId));
						plugin.bookshelves.remove(locStr);
						plugin.saveAll();
						player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_REMOVED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());
					}					
				}
				
			} else if (event.getAction() == Action.LEFT_CLICK_BLOCK && inHand != null && inHand.getType() == Material.BOOK && inHand.getDurability() != 0) {
				// placing book
				
				// check worldguard
				if (BookWorm.CHECK_WORLDGUARD && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, l)) {
					player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_PLACED_BOOK_FAIL);
					return;
				}
				
				// put book into bookshelf
				short bookId = inHand.getDurability();
				Book book = plugin.getBook(bookId);
				if (!book.isSaved()) book.save();
				plugin.bookshelves.put(locStr, bookId);
				plugin.saveAll(); // TODO: append instead of save all
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_PLACED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());
				
				// remove book in hand
				player.setItemInHand(null);
			}
		} else if (event.useItemInHand() != Result.DENY && event.getAction() == Action.RIGHT_CLICK_AIR && inHand.getType() == Material.BOOK && inHand.getDurability() != 0) {
			// reading the book in hand
			
			// get book
			Book book = plugin.getBook(inHand.getDurability());
			
			// get player bookmark
			Bookmark bookmark = plugin.bookmarks.get(player.getName());
			if (bookmark == null) {
				bookmark = new Bookmark();
				plugin.bookmarks.put(player.getName(), bookmark);
			}
			
			// set bookmark and read
			bookmark.readBook(player, book);
			
		}
	}
	
	@Override
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (item != null && item.getType() == Material.BOOK && item.getDurability() != 0) {
			Book book = plugin.books.get(item.getDurability());
			if (book == null) {
				book = new Book(item.getDurability());
				book.load();
			}
			event.getPlayer().sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + book.getTitle());
		}
	}

}
