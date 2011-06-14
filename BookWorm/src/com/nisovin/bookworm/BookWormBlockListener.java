package com.nisovin.bookworm;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;

public class BookWormBlockListener extends BlockListener {

	private BookWorm plugin;
	
	public BookWormBlockListener(BookWorm plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Priority.High, plugin);		
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.isCancelled() && event.getBlock().getType() == Material.BOOKSHELF) {
			Player player = event.getPlayer();
			Location l = event.getBlock().getLocation();
			String locStr = l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
			if (plugin.books.containsKey(locStr)) {
				// get book
				Book book = plugin.books.get(locStr);
				if (player.isOp() || book.getAuthor().equalsIgnoreCase(player.getName())) {
					// remove book
					plugin.books.remove(locStr);
					player.sendMessage(BookWorm.TEXT_COLOR + "Book destroyed: " + ChatColor.WHITE + book.getTitle());
					plugin.saveAll();
				} else {
					// someone else's book
					player.sendMessage(BookWorm.TEXT_COLOR + "You cannot destroy someone else's book!");
					event.setCancelled(true);
				}
			}
		}
	}
	
}
