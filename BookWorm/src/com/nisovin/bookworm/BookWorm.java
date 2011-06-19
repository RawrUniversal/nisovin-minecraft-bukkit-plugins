package com.nisovin.bookworm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class BookWorm extends JavaPlugin {

	protected static ChatColor TEXT_COLOR = ChatColor.GREEN;
	protected static int CLEAN_INTERVAL = 600;
	protected static int REMOVE_DELAY = 300;
	protected static boolean SHOW_TITLE_ON_HELD_CHANGE = true;
	protected static boolean REQUIRE_BOOK_TO_COPY = false;
	protected static boolean CHECK_WORLDGUARD = true;
	
	protected static int LINE_LENGTH = 55;
	protected static int PAGE_LENGTH = 6;
	protected static String INDENT = "    ";
	protected static String NEW_PARA = "::";
	
	protected static BookWorm plugin;
	protected PermissionManager perms;
	
	protected HashMap<Short,Book> books;
	protected HashMap<String,Short> bookshelves;
	protected HashMap<String,Bookmark> bookmarks;
	protected BookUnloader unloader;
	protected WorldGuardPlugin worldGuard;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		perms = new PermissionManager();
		books = new HashMap<Short,Book>();
		bookshelves = new HashMap<String,Short>();
		bookmarks = new HashMap<String,Bookmark>();
		
		loadBooks();
		loadConfig();
		
		new BookWormPlayerListener(this);
		new BookWormBlockListener(this);
		
		if (CLEAN_INTERVAL > 0) {
			unloader = new BookUnloader(this);
		}
		
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin != null) {
			worldGuard = (WorldGuardPlugin)plugin;
		}
		
		// prevent book stacking
		try {
			Field field = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
			field.setAccessible(true);
			field.set(net.minecraft.server.Item.BOOK, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender.isOp() && args.length == 1 && args[0].equals("-reload")) {
			for (Book book : books.values()) {
				if (!book.isSaved()) {
					book.save();
				}
			}
			books.clear();
			bookshelves.clear();
			bookmarks.clear();
			loadBooks();
			sender.sendMessage("BookWorm data reloaded.");
		} else if (sender instanceof Player) {
			Player player = (Player)sender;
			
			ItemStack inHand = player.getItemInHand();
			if (inHand == null || inHand.getType() != Material.BOOK) {
				player.sendMessage(TEXT_COLOR + "You must be holding a book to write.");
			} else if (args.length == 0) {	// show help
				if (inHand.getDurability() == 0) {
					player.sendMessage(TEXT_COLOR + "Use /" + label + " <title> to start your book.");
				} else {
					Book book = books.get(inHand.getDurability());
					if (book == null) {
						// book doesn't exist?
					} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
						// it's the author holding the book
						player.sendMessage(TEXT_COLOR + "Use /" + label + " <text> to add text to your book.");
						player.sendMessage(TEXT_COLOR + "You can use a double-colon :: to create a new paragraph.");
						player.sendMessage(TEXT_COLOR + "Right click on a bookcase to save your book.");
						player.sendMessage(TEXT_COLOR + "Type /" + label + " -help to see special commands.");
					} else {
						// it's someone else
						player.sendMessage(TEXT_COLOR + "Right click to read.");
					}
				}
			} else if (inHand.getDurability() == 0) {
				// starting new book
				short bookId = getNextBookId();
				if (bookId == -1) {
					// error, quit
					return true;
				}
				String title = "";
				for (int i = 0; i < args.length; i++) {
					title += args[i] + " ";
				}
				Book book = new Book(bookId, title.trim(), player.getName());
				books.put(bookId, book);
				inHand.setDurability(bookId);
				player.sendMessage(TEXT_COLOR + "New book created: " + ChatColor.WHITE + book.getTitle());
				// TODO: this block needs moved elsewhere
				/*if (args[0].startsWith("-")) {
					if (args[0].equalsIgnoreCase("-page") && args.length == 2 && args[1].matches("[0-9]+") && bookmarks.containsKey(player.getName())) {
						int page = Integer.parseInt(args[1]) - 1;
						if (page >= 0) {
							bookmarks.get(player.getName()).page = page;
							player.sendMessage(TEXT_COLOR + "Turned to page " + page + ".");
						}
					} else {
						player.sendMessage(TEXT_COLOR + "Invalid command.");
					}
				} else {*/
				//}
			} else {
				Book book = getBook(inHand.getDurability());
				if (args[0].startsWith("-")) {
					// special command
					if (args[0].equalsIgnoreCase("-help")) {
						player.sendMessage(TEXT_COLOR + "Special commands:");
						player.sendMessage(TEXT_COLOR + "   /" + label + " -read <page>" + ChatColor.WHITE + " -- read the specified page");
						//player.sendMessage(TEXT_COLOR + "   /" + label + " -title <title>" + ChatColor.WHITE + " -- change the title");
						player.sendMessage(TEXT_COLOR + "   /" + label + " -erase <text>" + ChatColor.WHITE + " -- erase the given text");
						player.sendMessage(TEXT_COLOR + "   /" + label + " -replace <old text> -> <new text>" + ChatColor.WHITE + " -- replace text");
						player.sendMessage(TEXT_COLOR + "   /" + label + " -eraseall" + ChatColor.WHITE + " -- erase the entire book");
						player.sendMessage(TEXT_COLOR + "   /" + label + " -cancel" + ChatColor.WHITE + " -- cancel book creation");
					} else if (args[0].equalsIgnoreCase("-read")) { 
						if (args.length == 2 && args[1].matches("[0-9]+")) {
							book.read(player, Integer.parseInt(args[1])-1);
						} else {
							book.read(player, 0);
						}
					} else if (perms.canModifyBook(player, book) && args[0].equalsIgnoreCase("-title") && args.length > 1) {
						//String title = "";
						//for (int i = 1; i < args.length; i++) {
						//	title += args[i] + " ";
						//}
						//book.setTitle(title.trim());
						//player.sendMessage(TEXT_COLOR + "Title changed: " + ChatColor.WHITE + title);
						player.sendMessage(TEXT_COLOR + "Title change option temporarily disabled.");
					} else if (perms.canModifyBook(player, book) && args[0].equalsIgnoreCase("-erase") && args.length > 1) {
						String s = "";
						for (int i = 1; i < args.length; i++) {
							s += args[i] + " ";
						}
						book.delete(s.trim());
						player.sendMessage(TEXT_COLOR + "String " + ChatColor.WHITE + s + TEXT_COLOR + " erased from book.");
					} else if (perms.canModifyBook(player, book) && args[0].equalsIgnoreCase("-replace") && args.length > 1) {
						String s = "";
						for (int i = 1; i < args.length; i++) {
							s += args[i] + " ";
						}
						s = s.trim();
						boolean replaced = book.replace(s.trim());
						if (replaced) {
							player.sendMessage(TEXT_COLOR + "String replaced.");
						} else {
							player.sendMessage(TEXT_COLOR + "String not found.");
						}
					} else if (args[0].equalsIgnoreCase("-eraseall")) {
						book.erase();
						player.sendMessage(TEXT_COLOR + "Book contents erased.");
					} else {
						player.sendMessage(TEXT_COLOR + "Invalid command.");
					}
				} else {
					// just writing
					if (perms.canModifyBook(player, book)) {
						String line = book.write(args);
						player.sendMessage(TEXT_COLOR + "Wrote: " +ChatColor.WHITE + line);
					} else {
						player.sendMessage(TEXT_COLOR + "You cannot write in a book that is not yours.");
					}
				}
			}
		}
		return true;
	}
	
	protected Book getBook(short id) {
		Book book = books.get(id);
		if (book == null) {
			book = new Book(id);
			book.load();
			books.put(id, book);
		}
		return book;
	}
	
	private void loadBooks() {
		try {
			Scanner scanner = new Scanner(new File(getDataFolder(), "bookshelves.txt"));
			while (scanner.hasNext()) {
				String[] line = scanner.nextLine().split(":");
				bookshelves.put(line[0], Short.parseShort(line[1]));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
		}
	}
	
	private void loadConfig() {
		Configuration config = getConfiguration();
		config.load();
		TEXT_COLOR = ChatColor.getByCode(config.getInt("general.text-color", ChatColor.GREEN.getCode()));
		LINE_LENGTH = config.getInt("formatting.line-length", LINE_LENGTH);
		PAGE_LENGTH = config.getInt("formatting.page-length", PAGE_LENGTH);
		int indent = config.getInt("formatting.indent-size", INDENT.length());
		INDENT = "";
		for (int i = 0; i < indent; i++) {
			INDENT += " ";
		}
		CLEAN_INTERVAL = config.getInt("general.clean-interval", CLEAN_INTERVAL);
		REMOVE_DELAY = config.getInt("general.remove-delay", REMOVE_DELAY);
		CHECK_WORLDGUARD = config.getBoolean("general.check-worldguard", CHECK_WORLDGUARD);
		SHOW_TITLE_ON_HELD_CHANGE = config.getBoolean("general.show-title-on-held-change", SHOW_TITLE_ON_HELD_CHANGE);
		REQUIRE_BOOK_TO_COPY = config.getBoolean("general.require-book-to-copy", REQUIRE_BOOK_TO_COPY);
		config.save();
	}
	
	protected void saveAll() {
		PrintWriter writer = null;
		try {
			// append entry to book list
			writer = new PrintWriter(new FileWriter(new File(BookWorm.plugin.getDataFolder(), "bookshelves.txt"), false));
			for (String s : bookshelves.keySet()) {
				writer.println(s + ":" + bookshelves.get(s));
			}
		} catch (IOException e) {
			getServer().getLogger().severe("BookWorm: Error writing bookshelf list");
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}
	
	protected short getNextBookId() {
		short id;
		File file = new File(getDataFolder(), "bookid.txt");
		if (!file.exists()) {
			id = 1;
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String s = reader.readLine();
				id = Short.parseShort(s);
				id++;
			} catch (Exception e) {
				return -1;
			} finally {
				try {
					if (reader != null) reader.close();
				} catch (Exception e) {
				}
			}
		}
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(file, false));
			writer.println(id);
		} catch (IOException e) {
			return -1;
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		return id;
	}
	
	@Override
	public void onDisable() {
		unloader.stop();
		for (Book book : books.values()) {
			if (!book.isSaved()) {
				book.save();
			}
		}
		books = null;
		bookshelves = null;
		bookmarks = null;
		unloader = null;
	}

}
