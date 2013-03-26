package com.nisovin.bookworm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nisovin.bookworm.event.BookCopyEvent;
import com.nisovin.bookworm.event.BookModifyEvent;
import com.nisovin.bookworm.event.BookModifyEvent.ModifyType;
import com.nisovin.bookworm.event.BookPlaceEvent;
import com.nisovin.bookworm.event.BookReadEvent;

class BookWormPlayerListener implements Listener {
	
	private BookWorm plugin;
	
	public BookWormPlayerListener(BookWorm plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if (plugin.chatModed.contains(player.getName()) && player.getItemInHand().getType() == Material.BOOK) {
			event.setCancelled(true);
			final String message = event.getMessage();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					Book book = BookWorm.getBook(player);
					if (book != null && plugin.perms.canModifyBook(player, book)) {
						BookModifyEvent evt = new BookModifyEvent(player, book, ModifyType.NEW_TEXT_WRITTEN, message);
						Bukkit.getPluginManager().callEvent(evt);
						if (evt.isCancelled()) {
							return;
						} else {
							String line = book.write(evt.getContent());
							player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_WRITE_DONE.replace("%t", BookWorm.TEXT_COLOR_2 + line));
						}
					}
				}
			});
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();
		if (!event.isCancelled() && (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType() == Material.BOOKSHELF) {
			onPlayerInteractBookshelf(event, player, inHand);
		} else if (event.useItemInHand() != Result.DENY && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && inHand.getType() == Material.BOOK && inHand.getDurability() != 0) {
			onPlayerInteractBook(event, player, inHand);
		}
	}
	
	private void onPlayerInteractBookshelf(PlayerInteractEvent event, Player player, ItemStack inHand) {
		Location l = event.getClickedBlock().getLocation();
		String locStr = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
		if (plugin.bookshelves.containsKey(locStr)) {
			// get book
			short bookId = plugin.bookshelves.get(locStr);
			Book book = plugin.getBookById(bookId);
			if (book == null) {
				// book doesn't exist - set the book in hand to durability 0
				inHand.setDurability((short)0);
				player.setItemInHand(inHand);
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
				BookReadEvent evt = new BookReadEvent(book, player, bookmark.page);
				plugin.callEvent(evt);
				if (!evt.isCancelled()) {
					
					// set bookmark and read
					bookmark.readBook(player, book);						
				}
									
			} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {	
				if (!player.isSneaking() && plugin.perms.canCopyBook(player, book) && ((!BookWorm.REQUIRE_BOOK_TO_COPY && inHand.getType() == Material.AIR) || (inHand != null && inHand.getType() == Material.BOOK && inHand.getDurability() == 0))) {				
					// copy book if allowed
					short copyId = bookId;
					
					// check listeners
					BookCopyEvent evt = new BookCopyEvent(player, book, l);
					Bukkit.getPluginManager().callEvent(evt);
					if (evt.isCancelled()) {
						event.setCancelled(true);
						return;
					}
					
					// copy book
					if (BookWorm.MAKE_REAL_COPY) {
						Book copy = plugin.copyBook(book);
						if (copy == null) return;
						copyId = copy.getId();
					}
					if (BookWorm.REQUIRE_BOOK_TO_COPY) {
						player.getItemInHand().setDurability(copyId);
					} else {
						player.setItemInHand(new ItemStack(Material.BOOK, 1, copyId));
					}
					player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COPIED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());
					event.setCancelled(true);
					
				} else if (player.isSneaking() && plugin.perms.canRemoveBook(player, book) && inHand.getType() == Material.AIR) {
					// remove book if allowed
					player.setItemInHand(new ItemStack(Material.BOOK, 1, bookId));
					plugin.bookshelves.remove(locStr);
					plugin.saveBookshelves();
					player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_REMOVED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());
					event.setCancelled(true);
				}					
			}
			
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !BookWorm.S_NO_BOOK.isEmpty()) {
			// reading from empty bookshelf
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_BOOK);
			
		} else if (event.getAction() == Action.LEFT_CLICK_BLOCK && inHand != null && inHand.getType() == Material.BOOK && inHand.getDurability() != 0) {
			// placing book
			
			// get book
			short bookId = inHand.getDurability();
			Book book = plugin.getBookById(bookId);
			if (book == null) {
				return;
			}
			
			// check permission
			if (!plugin.perms.canPlaceBook(player, book)) {
				return;
			}
			
			// check listeners
			BookPlaceEvent evt = new BookPlaceEvent(player, book, l);
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
				plugin.saveBookshelves();
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_PLACED_BOOK + " " + BookWorm.TEXT_COLOR_2 + book.getTitle());

				// remove book in hand
				player.setItemInHand(null);
				
				event.setCancelled(true);
			}
		}		
	}
	
	private void onPlayerInteractBook(PlayerInteractEvent event, Player player, ItemStack inHand) {
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
			// book doesn't exist - set the book in hand to durability 0
			inHand.setDurability((short)0);
			player.setItemInHand(inHand);
			return;
		}
		
		// set book title
		book.setBookMeta(inHand);
		
		// get player bookmark
		Bookmark bookmark = plugin.bookmarks.get(player.getName());
		if (bookmark == null) {
			bookmark = new Bookmark();
			plugin.bookmarks.put(player.getName(), bookmark);
		}				

		// check listeners
		BookReadEvent evt = new BookReadEvent(book, player, bookmark.page);
		plugin.callEvent(evt);
		if (!evt.isCancelled()) {
			// set bookmark and read
			bookmark.readBook(player, book);				
		}		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		if (!BookWorm.SHOW_TITLE_ON_HELD_CHANGE) return;
		ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (item != null && item.getType() == Material.BOOK && item.getDurability() != 0) {
			Book book = plugin.getBookById(item.getDurability());
			if (book != null) {
				event.getPlayer().sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + book.getTitle());
			} else {
				// book doesn't exist - set the book in hand to durability 0
				item.setDurability((short)0);
				event.getPlayer().getInventory().setItem(event.getNewSlot(), item);				
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack clicked = event.getCurrentItem();
		ItemStack cursor = event.getCursor();
		
		if (clicked != null && clicked.getType() == Material.BOOK && clicked.getDurability() != 0) {
			if (cursor != null && cursor.getType() == Material.BOOK && clicked.getDurability() != cursor.getDurability()) {
				// trying to stack different books - prevent it
				event.setCancelled(true);
			} else if (event.isShiftClick()) {
				// trying to shift move
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onPickupItem(PlayerPickupItemEvent event) {
		ItemStack item = event.getItem().getItemStack();
		if (item.getType() == Material.BOOK && item.getDurability() > 0) {
			event.setCancelled(true);
			Inventory inv = event.getPlayer().getInventory();
			int slot = inv.firstEmpty();
			if (slot >= 0) {
				inv.setItem(slot, item);
				event.getItem().remove();
			}
		}
	}

}
