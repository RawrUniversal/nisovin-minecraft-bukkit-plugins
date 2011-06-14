package com.nisovin.bookworm;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;

public class BookWormPlayerListener extends PlayerListener {
	
	private BookWorm plugin;
	
	public BookWormPlayerListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled() && event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.BOOKSHELF) {
			Player player = event.getPlayer();
			Location l = event.getClickedBlock().getLocation();
			String locStr = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
			if (plugin.books.containsKey(locStr)) {
				// get book
				Book book = plugin.books.get(locStr);
				
				// load into book if owner
				if (book.getAuthor().equalsIgnoreCase(player.getName()) && player.getItemInHand().getType() == Material.BOOK && !plugin.newBooks.containsKey(player.getName())) {
					NewBook newBook = new NewBook(book.getAuthor(), book.getTitle(), book.getContents());
					plugin.newBooks.put(player.getName(), newBook);
					player.sendMessage(BookWorm.TEXT_COLOR + "Copied book: " + ChatColor.WHITE + newBook.getTitle());
					return;
				}
				
				// get player bookmark
				Bookmark bookmark = plugin.bookmarks.get(player.getName());
				if (bookmark == null) {
					bookmark = new Bookmark();
					plugin.bookmarks.put(player.getName(), bookmark);
				}
				
				// set bookmark and read
				bookmark.readBook(player, book);
			} else if (player.getItemInHand().getType() == Material.BOOK && plugin.newBooks.containsKey(player.getName())) {
				// check worldguard
				if (plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, l)) {
					player.sendMessage(BookWorm.TEXT_COLOR + "You cannot put a book here.");
					return;
				}
				
				// get new book
				NewBook newBook = plugin.newBooks.get(player.getName());
				newBook.save(l);
				plugin.newBooks.remove(player.getName());
				player.sendMessage(BookWorm.TEXT_COLOR + "Book saved: " + ChatColor.WHITE + newBook.getTitle());
				
				// remove book in hand
				ItemStack inHand = player.getItemInHand();
				if (inHand.getAmount() == 1) {
					player.setItemInHand(null);
				} else {
					inHand.setAmount(inHand.getAmount()-1);
					player.setItemInHand(inHand);
				}
			}
		}
	}

}
