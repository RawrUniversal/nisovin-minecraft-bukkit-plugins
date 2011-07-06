package com.nisovin.bookworm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

import com.nisovin.bookworm.event.BookPlaceEvent;
import com.nisovin.bookworm.event.BookReadEvent;

public class BookWormPlayerListener extends PlayerListener {
	
	private BookWorm plugin;
	
	public BookWormPlayerListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_CHAT, this, Event.Priority.Low, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
		if (BookWorm.SHOW_TITLE_ON_HELD_CHANGE) {
			plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ITEM_HELD, this, Event.Priority.Monitor, plugin);
		}
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if (plugin.chatModed.contains(player.getName()) && player.getItemInHand().getType() == Material.BOOK) {
			Book book = BookWorm.getBook(player);
			if (book != null && plugin.perms.canModifyBook(player, book)) {
				String line = book.write(event.getMessage());
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_WRITE_DONE.replace("%t", BookWorm.TEXT_COLOR_2 + line));
				event.setCancelled(true);
			}
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
				Book book = plugin.getBookById(bookId);
				if (book == null) {
					return;
				}
				
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					// right click - just set bookmark and read
										
					// get player bookmark
					Bookmark bookmark = plugin.bookmarks.get(player.getName());
					if (bookmark == null) {
						bookmark = new Bookmark();
						plugin.bookmarks.put(player.getName(), bookmark);
					}					

					// check listeners
					BookReadEvent evt = new BookReadEvent("BOOK_READ", book, player, bookmark.page);
					plugin.callEvent(evt);
					if (!evt.isCancelled()) {
						
						// set bookmark and read
						bookmark.readBook(player, book);						
					}
										
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
				
				// get book
				short bookId = inHand.getDurability();
				Book book = plugin.getBookById(bookId);
				if (book == null) {
					return;
				}
				
				// check listeners
				BookPlaceEvent evt = new BookPlaceEvent("BOOK_PLACE", player, book, l);
				plugin.callEvent(evt);
				if (!evt.isCancelled()) {
					
					// check worldguard
					if (BookWorm.CHECK_WORLDGUARD && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, l)) {
						player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_PLACED_BOOK_FAIL);
						return;
					}
					
					// put book into bookshelf
					if (!book.isSaved()) book.save();
					plugin.bookshelves.put(locStr, bookId);
					plugin.saveAll(); // TODO: append instead of save all
					player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_PLACED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());
					
					// remove book in hand
					player.setItemInHand(null);
					
				}
			}
		} else if (event.useItemInHand() != Result.DENY && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && inHand.getType() == Material.BOOK && inHand.getDurability() != 0) {
			// reading the book in hand
			
			// check disallowed clicks
			if (event.hasBlock()) {
				Material m = event.getClickedBlock().getType();
				if (m == Material.CHEST || m == Material.WORKBENCH || m == Material.FURNACE || m == Material.DISPENSER) {
					return;
				}
			}
			
			// get book
			Book book = plugin.getBookById(inHand.getDurability());
			if (book == null) {
				return;
			}
			
			// get player bookmark
			Bookmark bookmark = plugin.bookmarks.get(player.getName());
			if (bookmark == null) {
				bookmark = new Bookmark();
				plugin.bookmarks.put(player.getName(), bookmark);
			}				

			// check listeners
			BookReadEvent evt = new BookReadEvent("BOOK_READ", book, player, bookmark.page);
			plugin.callEvent(evt);
			if (!evt.isCancelled()) {			
			
				// set bookmark and read
				bookmark.readBook(player, book);
				
			}
			
		}
	}
	
	@Override
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (item != null && item.getType() == Material.BOOK && item.getDurability() != 0) {
			Book book = plugin.getBookById(item.getDurability());
			if (book != null) {
				event.getPlayer().sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + book.getTitle());
			}
		}
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!event.isCancelled() && event.getItemDrop().getItemStack().getType() == Material.BOOK) {
			event.getPlayer().updateInventory();
		}
	}

}
