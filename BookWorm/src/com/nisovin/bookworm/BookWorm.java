package com.nisovin.bookworm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class BookWorm extends JavaPlugin {

	protected static ChatColor TEXT_COLOR = ChatColor.GREEN;
	
	protected static BookWorm plugin;
	protected HashMap<String,Book> books;
	protected HashMap<String,Bookmark> bookmarks;
	protected HashMap<String,NewBook> newBooks;
	protected BookUnloader unloader;
	protected WorldGuardPlugin worldGuard;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		loadBooks();
		
		new BookWormPlayerListener(this);
		new BookWormBlockListener(this);
		
		unloader = new BookUnloader(this);
		
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin != null) {
			worldGuard = (WorldGuardPlugin)plugin;
		}
		
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender.isOp() && args.length == 1 && args[0].equals("-reload")) {
			loadBooks();
			sender.sendMessage("BookWorm data reloaded.");
		} else if (sender instanceof Player) {
			Player player = (Player)sender;
			
			if (player.getItemInHand().getType() != Material.BOOK) {
				player.sendMessage(TEXT_COLOR + "You must be holding a book to write.");
			} else if (args.length == 0) {
				// must have args
				if (!newBooks.containsKey(player.getName())) {
					player.sendMessage(TEXT_COLOR + "Use /" + label + " <title> to start your book.");
				} else {
					player.sendMessage(TEXT_COLOR + "Use /" + label + " <text> to add text to your book.");
					player.sendMessage(TEXT_COLOR + "You can use a double-colon :: to create a new paragraph.");
					player.sendMessage(TEXT_COLOR + "Or use a command: -read <page>, -title <title>, -erase, -cancel.");
					player.sendMessage(TEXT_COLOR + "Example: /" + label + " -read 1 -- to read page 1.");
					player.sendMessage(TEXT_COLOR + "Right click on a bookcase to save your book.");
				}
			} else if (!newBooks.containsKey(player.getName())) {
				if (args[0].startsWith("-")) {
					player.sendMessage(TEXT_COLOR + "Invalid command.");
				} else {
					NewBook newBook = new NewBook(player, args);
					newBooks.put(player.getName(), newBook);
					player.sendMessage(TEXT_COLOR + "New book created: " + ChatColor.WHITE + newBook.getTitle());
				}
			} else {
				NewBook newBook = newBooks.get(player.getName());
				if (args[0].startsWith("-")) {
					// special command
					if (args[0].equalsIgnoreCase("-read")) { 
						if (args.length == 2 && args[1].matches("[0-9]+")) {
							newBook.read(player, Integer.parseInt(args[1])-1);
						} else {
							newBook.read(player, 0);
						}
					} else if (args[0].equalsIgnoreCase("-title") && args.length > 1) {
						String title = "";
						for (int i = 1; i < args.length; i++) {
							title += args[i] + " ";
						}
						newBook.setTitle(title.trim());
						player.sendMessage(TEXT_COLOR + "Title changed: " + ChatColor.WHITE + title);
					} else if (args[0].equalsIgnoreCase("-erase")) {
						newBook.erase();
						player.sendMessage(TEXT_COLOR + "Book contents erased.");
					} else if (args[0].equalsIgnoreCase("-cancel")) {
						newBooks.remove(player.getName());
						player.sendMessage(TEXT_COLOR + "Book cancelled.");
					} else {
						player.sendMessage(TEXT_COLOR + "Invalid command.");
					}
				} else {
					// just writing
					String line = newBook.write(args);
					player.sendMessage(TEXT_COLOR + "Wrote: " +ChatColor.WHITE + line);
				}
			}
		}
		return true;
	}
	
	private void loadBooks() {		
		books = new HashMap<String,Book>();
		bookmarks = new HashMap<String,Bookmark>();
		newBooks = new HashMap<String,NewBook>();
		
		try {
			Scanner scanner = new Scanner(new File(getDataFolder(), "books.txt"));
			while (scanner.hasNext()) {
				String[] line = scanner.nextLine().split(":");
				books.put(line[0], new Book(line[1]));
			}
			scanner.close();
		} catch (FileNotFoundException e) {			
		}
	}
	
	protected void saveAll() {
		PrintWriter writer = null;
		try {
			// append entry to book list
			writer = new PrintWriter(new FileWriter(new File(BookWorm.plugin.getDataFolder(), "books.txt"), false));
			for (String s : books.keySet()) {
				writer.println(s + ":" + books.get(s).getId());
			}
		} catch (IOException e) {
			getServer().getLogger().severe("BookWorm: Error writing book list");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	@Override
	public void onDisable() {
		unloader.stop();
		books = null;
		bookmarks = null;
		newBooks = null;
		unloader = null;
	}

}
