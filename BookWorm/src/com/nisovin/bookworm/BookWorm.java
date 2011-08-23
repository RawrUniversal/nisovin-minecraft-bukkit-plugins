package com.nisovin.bookworm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

import com.nisovin.bookworm.event.*;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class BookWorm extends JavaPlugin {

	protected static String STACK_BY_DATA_VAR = "bj";
	
	protected static ChatColor TEXT_COLOR = ChatColor.GREEN;
	protected static ChatColor TEXT_COLOR_2 = ChatColor.WHITE;
	
	protected static int LINE_LENGTH = 55;
	protected static int PAGE_LENGTH = 6;
	protected static String INDENT = "    ";
	protected static String NEW_PARA = "::";
	
	protected static boolean SHOW_TITLE_ON_HELD_CHANGE = true;
	protected static boolean REQUIRE_BOOK_TO_COPY = false;
	protected static boolean MAKE_REAL_COPY = false;
	protected static boolean CHECK_WORLDGUARD = true;
	protected static boolean USE_FULL_FILENAMES = true;
	protected static boolean AUTO_CHAT_MODE = true;
	protected static boolean BOOK_INFO_ACHIEVEMENT = true;
	protected static boolean DROP_BOOKSHELF = false;
	protected static boolean KEEP_ALL_BOOKS_LOADED = false;
	protected static boolean SPOUT_ENABLED = false;
	
	protected static int CLEAN_INTERVAL = 600;
	protected static int REMOVE_DELAY = 300;
		
	protected static String S_MUST_HOLD_BOOK = "You must be holding a single book to write.";
	protected static String S_USAGE_START = "Use /%c <title> to start your book.";
	protected static String S_USAGE_WRITE = 
			"Use /%c <text> to add text to your book.\n" +
			"You can use a double-colon :: to create a new paragraph.\n" +
			"Left click on a bookcase to place your book.\n" +
			"Type /%c -help to see special commands.";
	protected static String S_USAGE_READ = "Right click to read.";
	protected static String S_NEW_BOOK_CREATED = "New book created: %t";
	protected static String S_CANNOT_DESTROY = "You can't destroy someone else's bookshelf!";
	
	protected static String S_COMM_HELP = "help";
	protected static String S_COMM_READ = "read";
	protected static String S_COMM_TITLE = "title";
	protected static String S_COMM_UNDO = "undo";
	protected static String S_COMM_ERASE = "erase";
	protected static String S_COMM_REPLACE = "replace";
	protected static String S_COMM_ERASEALL = "eraseall";	
	protected static String S_COMM_CHATMODE = "chat";

	protected static String S_COMM_HELP_TEXT = "Special commands:\n" +
			"   /%c -chat -- toggle chat write mode\n" +
			"   /%c -read <page> -- read the specified page\n" +
			"   /%c -undo -- undo the last write action\n" +
			"   /%c -erase <text> -- erase the given text\n" +
			"   /%c -replace <old text> -> <new text> -- replace text\n" +
			"   /%c -title <new title> -- change the book's title\n" +
			"   /%c -eraseall -- erase the entire book";	
	protected static String S_COMM_UNDO_DONE = "Undo successful.";
	protected static String S_COMM_UNDO_FAIL = "Unable to undo.";
	protected static String S_COMM_ERASE_DONE = "Text erased.";
	protected static String S_COMM_REPLACE_DONE = "Text replaced.";
	protected static String S_COMM_REPLACE_FAIL = "Text not found.";
	protected static String S_COMM_ERASEALL_DONE = "Book contents erased.";
	protected static String S_COMM_CHATMODE_ON = "Chat write mode enabled.";
	protected static String S_COMM_CHATMODE_OFF = "Chat write mode disabled.";
	protected static String S_COMM_INVALID = "Invalid command.";
	
	protected static String S_WRITE_DONE = "Wrote line: %t";
	protected static String S_WRITE_FAIL = "You cannot write in a book that is not yours.";
	
	protected static String S_READ_DIVIDER = "--------------------------------------------------";
	protected static String S_READ_BOOK = "Book";
	protected static String S_READ_BY = "by";
	protected static String S_READ_PAGE = "page";
	protected static String S_COPIED_BOOK = "Copied book:";
	protected static String S_REMOVED_BOOK = "Removed book:";
	protected static String S_PLACED_BOOK = "Book placed in bookshelf:";
	protected static String S_PLACED_BOOK_FAIL = "You cannot put a book here.";
	protected static String S_NO_PERMISSION = "You do not have permission to do that.";
		
	protected static BookWorm plugin;
	protected PermissionManager perms;
	
	protected HashMap<Short,Book> books;
	protected HashMap<String,Short> bookshelves;
	protected HashMap<String,Bookmark> bookmarks;
	protected HashSet<String> chatModed;
	protected BookUnloader unloader;
	protected WorldGuardPlugin worldGuard;
	protected HashSet<BookWormListener> listeners;
	
	@Override
	public void onEnable() {
		plugin = this;
		
		// make sure save folder exists
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}
		
		// setup storage
		perms = new PermissionManager();
		listeners = new HashSet<BookWormListener>();
		books = new HashMap<Short,Book>();
		bookshelves = new HashMap<String,Short>();
		bookmarks = new HashMap<String,Bookmark>();
		chatModed = new HashSet<String>();
		
		// load up stuff
		loadBooks();
		loadConfig();
		
		// register listeners
		new BookWormPlayerListener(this);
		new BookWormBlockListener(this);
		new BookWormWorldListener(this);
		
		// check for spout
		Plugin spout = getServer().getPluginManager().getPlugin("Spout");
		if (spout != null) {
			SPOUT_ENABLED = true;
			new BookWormSpoutInventoryListener(this);
			getServer().getLogger().info("BookWorm 'Spout' support enabled.");
		} else {
			SPOUT_ENABLED = false;
		}
		
		// start memory cleaner
		if (CLEAN_INTERVAL > 0 && !KEEP_ALL_BOOKS_LOADED) {
			unloader = new BookUnloader(this);
		}
		
		// load all books
		if (KEEP_ALL_BOOKS_LOADED) {
			short maxId = getCurrentBookId();
			if (maxId > 0) {
				for (short i = 0; i < maxId; i++) {
					getBookById(i);
				}
			}
		}
		
		// initialize world guard
		if (CHECK_WORLDGUARD) {
			Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				worldGuard = (WorldGuardPlugin)plugin;
			}
		}
		
		// prevent book stacking
		try {
			boolean ok = false;
			try {
				// attempt to make books with different data values stack separately
				Field field1 = net.minecraft.server.Item.class.getDeclaredField(STACK_BY_DATA_VAR);
				if (field1.getType() == boolean.class) {
					field1.setAccessible(true);
					field1.setBoolean(net.minecraft.server.Item.BOOK, true);
					ok = true;
				} 
			} catch (Exception e) {
			}
			if (!ok) {
				// otherwise limit stack size to 1
				Field field2 = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
				field2.setAccessible(true);
				field2.setInt(net.minecraft.server.Item.BOOK, 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		getServer().getLogger().info("BookWorm v" + this.getDescription().getVersion() + " loaded!");
		
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender.hasPermission("bookworm.reload") && args.length == 1 && args[0].equalsIgnoreCase("-reload")) {
			for (Book book : books.values()) {
				if (!book.isSaved()) {
					book.save();
				}
			}
			books.clear();
			bookshelves.clear();
			bookmarks.clear();
			loadBooks();
			loadConfig();
			sender.sendMessage("BookWorm data reloaded.");
		} else if (sender.hasPermission("bookworm.list") && args.length >= 1 && args[0].equalsIgnoreCase("-list")) {
			ArrayList<Book> bookList = new ArrayList<Book>();
			bookList.addAll(books.values());
			Collections.sort(bookList, new Comparator<Book>() {
				public int compare(Book book1, Book book2) {
					return book1.getId() - book2.getId();
				}
			});
			
			int page = 0;
			if (args.length > 1) {
				page = Integer.parseInt(args[1]) - 1;
			}
			
			int BOOKS_PER_PAGE = 10;
			Book b;
			for (int i = page*BOOKS_PER_PAGE; i < page*BOOKS_PER_PAGE+BOOKS_PER_PAGE && i < bookList.size(); i++) {
				b = bookList.get(i);
				sender.sendMessage(b.getId() + " - " + b.getAuthor() + " - " + b.getTitle());
			}
		} else if (sender.hasPermission("bookworm.search") && args.length > 2 && args[0].equalsIgnoreCase("-search")) {
			ArrayList<Book> results = new ArrayList<Book>();
			if (args[1].equalsIgnoreCase("author")) {
				String author = args[2].toLowerCase();
				for (Book book : books.values()) {
					if (book.getAuthor().toLowerCase().contains(author)) {
						results.add(book);
					}
				}
			} else if (args[1].equalsIgnoreCase("title")) {
				String title = "";
				for (int i = 2; i < args.length; i++) {
					title += args[i].toLowerCase() + " ";
				}
				title = title.trim();
				for (Book book : books.values()) {
					if (book.getTitle().toLowerCase().contains(title)) {
						results.add(book);
					}
				}
			}
		} else if (sender instanceof Player) {
			Player player = (Player)sender;
			ItemStack inHand = player.getItemInHand();
			if (inHand == null || inHand.getType() != Material.BOOK || inHand.getAmount() != 1) {
				player.sendMessage(TEXT_COLOR + S_MUST_HOLD_BOOK);
			} else if (args.length == 0) {	// show help
				if (inHand.getDurability() == 0) {
					player.sendMessage(TEXT_COLOR + S_USAGE_START.replace("%c", label));
				} else {
					Book book = getBookById(inHand.getDurability());
					if (book == null) {
						// book doesn't exist?
						return true;
					} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
						// it's the author holding the book
						String[] lines = S_USAGE_WRITE.split("\n");
						for (String line : lines) {
							if (!line.equals("")) {
								player.sendMessage(TEXT_COLOR + line.replace("%c", label));
							}
						}
					} else {
						// it's someone else
						player.sendMessage(TEXT_COLOR + S_USAGE_READ.replace("%c", label));
					}
				}
			} else if (inHand.getDurability() == 0) {
				// starting new book
				if (inHand.getAmount() == 1 && perms.canCreateBook(player)) {
					short bookId = getNextBookId();
					if (bookId == -1) {
						// error, quit
						return true;
					}

					int titleStart = 0;
					
					// check for chat mode
					boolean chatMode = false;
					if (args[0].equalsIgnoreCase("-" + S_COMM_CHATMODE)) {
						titleStart = 1;
						chatMode = true;
					} else if (AUTO_CHAT_MODE) {
						chatMode = true;
					}
					if (chatMode) {
						chatModed.add(player.getName());					
					}
					
					// get title
					String title = "";
					for (int i = titleStart; i < args.length; i++) {
						title += args[i] + " ";
					}
					
					// setup book
					Book book = new Book(bookId, title.trim(), player.getName());
					books.put(bookId, book);
					inHand.setDurability(bookId);
					
					// send messages
					player.sendMessage(TEXT_COLOR + S_NEW_BOOK_CREATED.replace("%t", TEXT_COLOR_2 + book.getTitle()));
					if (chatMode) {
						player.sendMessage(TEXT_COLOR + S_COMM_CHATMODE_ON);	
					}
				} else {
					player.sendMessage(TEXT_COLOR + S_NO_PERMISSION);
				}
			} else {
				Book book = getBookById(inHand.getDurability());
				if (book == null) {
					return true;
				}
				if (args[0].startsWith("-")) {
					// special command
					if (args[0].equalsIgnoreCase("-" + S_COMM_HELP)) {
						String[] lines = S_COMM_HELP_TEXT.split("\n");
						for (String line : lines) {
							if (!line.equals("")) {
								player.sendMessage(TEXT_COLOR + line.replace("%c", label).replace("--", TEXT_COLOR_2 + "--"));
							}
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_READ)) { 
						if (args.length == 2 && args[1].matches("[0-9]+")) {
							book.read(player, Integer.parseInt(args[1])-1);
						} else {
							book.read(player, 0);
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_TITLE) && args.length > 1) {
						if (perms.canModifyBook(player, book)) {
							String title = "";
							for (int i = 1; i < args.length; i++) {
								title += args[i] + " ";
							}
							book.setTitle(title.trim());
							player.sendMessage(TEXT_COLOR + "Title changed: " + TEXT_COLOR_2 + title);							
						} else {
							player.sendMessage(TEXT_COLOR + S_NO_PERMISSION);							
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_UNDO)) {
						if (perms.canModifyBook(player, book)) {
							boolean undone = book.undo();
							if (undone) {
								player.sendMessage(TEXT_COLOR + S_COMM_UNDO_DONE);
							} else {
								player.sendMessage(TEXT_COLOR + S_COMM_UNDO_FAIL);
							}
						} else {
							player.sendMessage(TEXT_COLOR + S_NO_PERMISSION);
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_ERASE) && args.length > 1) {
						if (perms.canModifyBook(player, book)) {
							String s = "";
							for (int i = 1; i < args.length; i++) {
								s += args[i] + " ";
							}
							book.erase(s.trim());
							player.sendMessage(TEXT_COLOR + S_COMM_ERASE_DONE);
						} else {
							player.sendMessage(TEXT_COLOR + S_NO_PERMISSION);								
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_REPLACE) && args.length > 1) {
						if (perms.canModifyBook(player, book)) {
							String s = "";
							for (int i = 1; i < args.length; i++) {
								s += args[i] + " ";
							}
							s = s.trim();
							boolean replaced = book.replace(s.trim());
							if (replaced) {
								player.sendMessage(TEXT_COLOR + S_COMM_REPLACE_DONE);
							} else {
								player.sendMessage(TEXT_COLOR + S_COMM_REPLACE_FAIL);
							}
						} else {
							player.sendMessage(TEXT_COLOR + S_NO_PERMISSION);							
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_ERASEALL)) {
						if (perms.canModifyBook(player, book)) {
							book.eraseAll();
							player.sendMessage(TEXT_COLOR + S_COMM_ERASEALL_DONE);
						} else {
							player.sendMessage(TEXT_COLOR + S_NO_PERMISSION);			
						}
					} else if (args[0].equalsIgnoreCase("-" + S_COMM_CHATMODE)) {
						int mode = -1;
						if (args.length > 1) {
							if (args[1].equalsIgnoreCase("on")) {
								mode = 1;
							} else if (args[1].equalsIgnoreCase("off")) {
								mode = 0;
							}
						} else if (chatModed.contains(player.getName())) {
							mode = 0;
						} else {	
							mode = 1;
						}
						if (mode == 1) {
							chatModed.add(player.getName());
							player.sendMessage(TEXT_COLOR + S_COMM_CHATMODE_ON);							
						} else if (mode == 0) {
							chatModed.remove(player.getName());
							player.sendMessage(TEXT_COLOR + S_COMM_CHATMODE_OFF);							
						} else {
							player.sendMessage(TEXT_COLOR + S_COMM_INVALID);							
						}
					} else {
						player.sendMessage(TEXT_COLOR + S_COMM_INVALID);
					}
				} else {
					// just writing
					if (perms.canModifyBook(player, book)) {
						String line = book.write(args);
						player.sendMessage(TEXT_COLOR + S_WRITE_DONE.replace("%t", TEXT_COLOR_2 + line));
					} else {
						player.sendMessage(TEXT_COLOR + S_WRITE_FAIL);
					}
				}
			}
		}
		return true;
	}
	
	public static Book getBook(Player player) {
		return getBook(player.getItemInHand());
	}
	
	public static Book getBook(ItemStack item) {
		if (item == null || item.getType() != Material.BOOK || item.getAmount() != 1 || item.getDurability() == 0) {
			return null;
		} else {
			return plugin.getBookById(item.getDurability());
		}
	}
	
	public static Book getBook(short id) {
		return plugin.getBookById(id);
	}
	
	protected Book getBookById(short id) {
		Book book = books.get(id);
		if (book == null) {
			book = new Book(id);
			if (book.load()) {
				books.put(id, book);
			} else {
				book = null;
			}
		}
		return book;
	}
	
	protected Book copyBook(Book book) {
		short id = getNextBookId();
		if (id == -1) {
			return null;
		}
		Book copy = new Book(id, book);
		books.put(id, copy);
		return copy;
	}
	
	public static void registerListener(BookWormListener listener) {
		plugin.listeners.add(listener);
	}
	
	public static void unregisterListener(BookWormListener listener) {
		plugin.listeners.remove(listener);
	}
	
	protected void callEvent(BookEvent event) {
		if (event instanceof BookReadEvent) {
			for (BookWormListener listener : listeners) {
				listener.onBookRead((BookReadEvent)event);
			}
		} else if (event instanceof BookPlaceEvent) {
			for (BookWormListener listener : listeners) {
				listener.onBookPlace((BookPlaceEvent)event);
			}			
		}
	}
	
	public static PermissionManager getPermissions() {
		return plugin.perms;
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
		
		STACK_BY_DATA_VAR = config.getString("general.secret-amazing-code-do-not-change", STACK_BY_DATA_VAR);
		
		TEXT_COLOR = ChatColor.getByCode(config.getInt("general.text-color", TEXT_COLOR.getCode()));
		TEXT_COLOR_2 = ChatColor.getByCode(config.getInt("general.text-color-2", TEXT_COLOR_2.getCode()));
		
		SHOW_TITLE_ON_HELD_CHANGE = config.getBoolean("general.show-title-on-held-change", SHOW_TITLE_ON_HELD_CHANGE);
		REQUIRE_BOOK_TO_COPY = config.getBoolean("general.require-book-to-copy", REQUIRE_BOOK_TO_COPY);
		MAKE_REAL_COPY = config.getBoolean("general.make-real-copy", MAKE_REAL_COPY);
		AUTO_CHAT_MODE = config.getBoolean("general.auto-chat-mode", AUTO_CHAT_MODE);
		BOOK_INFO_ACHIEVEMENT = config.getBoolean("general.book-info-achievement", BOOK_INFO_ACHIEVEMENT);
		DROP_BOOKSHELF = config.getBoolean("general.drop-bookshelf-on-break", DROP_BOOKSHELF);
		KEEP_ALL_BOOKS_LOADED = config.getBoolean("general.keep-all-books-loaded", KEEP_ALL_BOOKS_LOADED);

		CHECK_WORLDGUARD = config.getBoolean("general.check-worldguard", CHECK_WORLDGUARD);
		USE_FULL_FILENAMES = config.getBoolean("general.use-full-filenames", USE_FULL_FILENAMES);
		CLEAN_INTERVAL = config.getInt("general.clean-interval", CLEAN_INTERVAL);
		REMOVE_DELAY = config.getInt("general.remove-delay", REMOVE_DELAY);
		
		LINE_LENGTH = config.getInt("formatting.line-length", LINE_LENGTH);
		PAGE_LENGTH = config.getInt("formatting.page-length", PAGE_LENGTH);
		int indent = config.getInt("formatting.indent-size", INDENT.length());
		INDENT = "";
		for (int i = 0; i < indent; i++) {
			INDENT += " ";
		}		
		
		S_MUST_HOLD_BOOK = config.getString("strings.must-hold-book", S_MUST_HOLD_BOOK);
		S_USAGE_START = config.getString("strings.usage-start", S_USAGE_START);
		S_USAGE_WRITE = config.getString("strings.usage-write", S_USAGE_WRITE);
		S_USAGE_READ = config.getString("strings.usage-read", S_USAGE_READ);
		S_NEW_BOOK_CREATED = config.getString("strings.new-book-created", S_NEW_BOOK_CREATED);
		S_CANNOT_DESTROY = config.getString("strings.cannot-destroy", S_CANNOT_DESTROY);
		
		S_COMM_HELP = config.getString("strings.command-help", S_COMM_HELP);
		S_COMM_READ = config.getString("strings.command-read", S_COMM_READ);
		S_COMM_TITLE = config.getString("strings.command-title", S_COMM_TITLE);
		S_COMM_UNDO = config.getString("strings.command-undo", S_COMM_UNDO);
		S_COMM_ERASE = config.getString("strings.command-erase", S_COMM_ERASE);
		S_COMM_REPLACE = config.getString("strings.command-replace", S_COMM_REPLACE);
		S_COMM_ERASEALL = config.getString("strings.command-eraseall", S_COMM_ERASEALL);
		S_COMM_CHATMODE = config.getString("strings.command-chatmode", S_COMM_CHATMODE);
		
		S_COMM_HELP_TEXT = config.getString("strings.command-help-text", S_COMM_HELP_TEXT);
		S_COMM_UNDO_DONE = config.getString("strings.command-undo-done", S_COMM_UNDO_DONE);
		S_COMM_UNDO_FAIL = config.getString("strings.command-undo-fail", S_COMM_UNDO_FAIL);
		S_COMM_ERASE_DONE = config.getString("strings.command-erase-done", S_COMM_ERASE_DONE);
		S_COMM_REPLACE_DONE = config.getString("strings.command-replace-done", S_COMM_REPLACE_DONE);
		S_COMM_REPLACE_FAIL = config.getString("strings.command-replace-fail", S_COMM_REPLACE_FAIL);
		S_COMM_ERASEALL_DONE = config.getString("strings.command-eraseall-done", S_COMM_ERASEALL_DONE);
		S_COMM_CHATMODE_ON = config.getString("strings.command-chatmode-on", S_COMM_CHATMODE_ON);
		S_COMM_CHATMODE_OFF = config.getString("strings.command-chatmode-off", S_COMM_CHATMODE_OFF);
		S_COMM_INVALID = config.getString("strings.command-invalid", S_COMM_INVALID);
		
		S_WRITE_DONE = config.getString("strings.write-done", S_WRITE_DONE);
		S_WRITE_FAIL = config.getString("strings.write-fail", S_WRITE_FAIL);
		
		S_READ_DIVIDER = config.getString("strings.read-divider", S_READ_DIVIDER);
		S_READ_BOOK = config.getString("strings.book", S_READ_BOOK);
		S_READ_BY = config.getString("strings.by", S_READ_BY);
		S_READ_PAGE = config.getString("strings.page", S_READ_PAGE);
		
		S_COPIED_BOOK = config.getString("strings.copied-book", S_COPIED_BOOK);
		S_REMOVED_BOOK = config.getString("strings.removed-book", S_REMOVED_BOOK);
		S_PLACED_BOOK = config.getString("strings.placed-book", S_PLACED_BOOK);
		S_PLACED_BOOK_FAIL = config.getString("strings.placed-book-fail", S_PLACED_BOOK_FAIL);
		S_NO_PERMISSION = config.getString("strings.no-permission", S_NO_PERMISSION);
		
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
		short id = (short) (getCurrentBookId() + 1);
		if (id > 0) {
			File file = new File(getDataFolder(), "bookid.txt");
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
		}
		return id;
	}
	
	protected short getCurrentBookId() {
		short id;
		File file = new File(getDataFolder(), "bookid.txt");
		if (!file.exists()) {
			id = 0;
		} else {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
				String s = reader.readLine();
				id = Short.parseShort(s);
			} catch (Exception e) {
				id = -1;
			} finally {
				try {
					if (reader != null) reader.close();
				} catch (Exception e) {
				}
			}
		}
		return id;
	}
	
	@Override
	public void onDisable() {
		if (unloader != null) {
			unloader.stop();
		}
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
