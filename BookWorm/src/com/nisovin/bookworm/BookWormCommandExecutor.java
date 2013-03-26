package com.nisovin.bookworm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.bookworm.event.BookModifyEvent;
import com.nisovin.bookworm.event.BookModifyEvent.ModifyType;

class BookWormCommandExecutor implements CommandExecutor {

	private BookWorm plugin;
	
	public BookWormCommandExecutor(BookWorm plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender.hasPermission("bookworm.reload") && args.length == 1 && args[0].equalsIgnoreCase("-reload")) {
			reload(sender, args);
		} else if (sender.hasPermission("bookworm.list") && args.length >= 1 && args[0].equalsIgnoreCase("-list")) {
			list(sender, args);
		} else if (sender.hasPermission("bookworm.search") && args.length > 2 && args[0].equalsIgnoreCase("-search")) {
			search(sender, args);
		} else if (sender.hasPermission("bookworm.delete") && args.length >= 1 && args[0].equalsIgnoreCase("-delete")) {
			delete(sender, label, args);
		} else if (sender.hasPermission("bookworm.getid") && sender instanceof Player && args.length == 1 && args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_ID)) {
			id((Player)sender);
		} else if (sender instanceof Player && args.length == 2 && args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_GET)) {
			get((Player)sender, args);
		} else if (sender instanceof Player && args.length == 1 && args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_HELP)) {
			help((Player)sender, label);
		} else if (sender instanceof Player) {
			// get player and item in hand
			Player player = (Player)sender;
			ItemStack inHand = player.getItemInHand();
			if (inHand == null || inHand.getType() != Material.BOOK || inHand.getAmount() != 1) {
				// not holding book
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_MUST_HOLD_BOOK);
			} else if (args.length == 0) {
				// no args, show command usage
				showUsage(player, inHand, label);
			} else if (inHand.getDurability() == 0) {
				// starting a new book
				newBook(player, inHand, args);
			} else {
				// get book affected
				Book book = plugin.getBookById(inHand.getDurability());
				if (book == null) {
					// book doesn't exist - start a new book
					newBook(player, inHand, args);
					return true;
				}
				if (args[0].startsWith("-")) {
					// special command
					if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_READ)) {
						read(player, book, args);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_TITLE) && args.length > 1) {
						title(player, book, args);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_AUTHOR) && args.length > 1) {
						author(player, book, args);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_UNDO)) {
						undo(player, book);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_ERASE) && args.length > 1) {
						erase(player, book, args);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_REPLACE) && args.length > 1) {
						replace(player, book, args);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_ERASEALL)) {
						eraseAll(player, book);
					} else if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_CHATMODE)) {
						chatMode(player, args);
					} else {
						player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_INVALID);
					}
				} else {
					// just writing
					if (plugin.perms.canModifyBook(player, book)) {
						String text = "";
						for (String arg : args) {
							text += arg + " ";
						}
						BookModifyEvent evt = new BookModifyEvent(player, book, ModifyType.NEW_TEXT_WRITTEN, text.trim());
						Bukkit.getPluginManager().callEvent(evt);
						if (!evt.isCancelled()) {
							String line = book.write(evt.getContent());
							player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_WRITE_DONE.replace("%t", BookWorm.TEXT_COLOR_2 + line));
						} else {
							player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_WRITE_FAIL);
						}
					} else {
						player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_WRITE_FAIL);
					}
				}
			}
		} else {
			sender.sendMessage(BookWorm.S_COMM_INVALID);
		}
		return true;
	}
	
	private void reload(CommandSender sender, String[] args) {
		for (Book book : plugin.books.values()) {
			if (!book.isSaved()) {
				book.save();
			}
		}
		plugin.books.clear();
		plugin.bookshelves.clear();
		plugin.bookmarks.clear();
		plugin.extraBookIds.clear();
		plugin.loadConfig();
		plugin.loadBookshelves();
		plugin.loadExtraBookIds();
		sender.sendMessage("BookWorm data reloaded.");		
	}
	
	private void list(CommandSender sender, String[] args) {
		ArrayList<Book> bookList = new ArrayList<Book>();
		bookList.addAll(plugin.books.values());
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
	}
	
	private void search(CommandSender sender, String[] args) {
		ArrayList<Book> results = new ArrayList<Book>();
		if (args[1].equalsIgnoreCase("author")) {
			String author = args[2].toLowerCase();
			for (Book book : plugin.books.values()) {
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
			for (Book book : plugin.books.values()) {
				if (book.getTitle().toLowerCase().contains(title)) {
					results.add(book);
				}
			}
		}
	}
	
	private void delete(CommandSender sender, String label, String[] args) {
		// show usage
		if (args.length == 1) {
			ItemStack item = null;
			if (sender instanceof Player) {
				item = ((Player)sender).getItemInHand();
				if (item.getType() != Material.BOOK || item.getDurability() == 0) {
					item = null;
				}
			}				
			if (item == null) {
				sender.sendMessage("You must specify a book id.");
			} else {
				sender.sendMessage("To delete the book you are holding, type:");
				sender.sendMessage("/" + label + " -delete " + item.getDurability());
			}
			return;
		}
		
		// get book
		short id = Short.parseShort(args[1]);
		Book book = plugin.getBookById(id);
		if (book == null) {
			// fail - book doesn't exist
			sender.sendMessage("Book id does not exist.");
			return;
		}
		
		// delete book
		boolean success = book.delete();
		if (!success) {
			sender.sendMessage("Failed to delete book.");
		} else {
			// remove book from list
			plugin.books.remove(id);
			// remove bookmarks
			Iterator<Map.Entry<String,Bookmark>> bookmarkIter = plugin.bookmarks.entrySet().iterator();
			while (bookmarkIter.hasNext()) {
				Map.Entry<String, Bookmark> entry = bookmarkIter.next();
				if (entry.getValue().book.getId() == id) {
					bookmarkIter.remove();
				}
			}
			// remove from bookshelves
			boolean removedShelves = false;
			Iterator<Map.Entry<String,Short>> bookshelfIter = plugin.bookshelves.entrySet().iterator();
			while (bookshelfIter.hasNext()) {
				Map.Entry<String,Short> entry = bookshelfIter.next();
				if (entry.getValue().equals(id)) {
					bookshelfIter.remove();
					removedShelves = true;
				}
			}
			if (removedShelves) {
				plugin.saveBookshelves();
			}
			// add to extra book ids
			plugin.extraBookIds.add(id);
			plugin.saveExtraBookIds();
			// send message
			sender.sendMessage("Book " + id + " deleted.");
		}
	}
	
	private void id(Player player) {
		ItemStack inHand = player.getItemInHand();
		if (inHand.getType() == Material.BOOK) {
			short id = inHand.getDurability();
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_ID_DONE + BookWorm.TEXT_COLOR_2 + id);
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_ID_FAIL);
		}
	}
	
	private void showUsage(Player player, ItemStack inHand, String label) {
		if (inHand.getDurability() == 0) {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_USAGE_START.replace("%c", label));
		} else {
			Book book = plugin.getBookById(inHand.getDurability());
			if (book == null) {
				// book doesn't exist?
				return;
			} else if (book.getAuthor().equalsIgnoreCase(player.getName())) {
				// it's the author holding the book
				String[] lines = BookWorm.S_USAGE_WRITE.split("\n");
				for (String line : lines) {
					if (!line.equals("")) {
						player.sendMessage(BookWorm.TEXT_COLOR + line.replace("%c", label));
					}
				}
			} else {
				// it's someone else
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_USAGE_READ.replace("%c", label));
			}
		}		
	}
	
	private void newBook(Player player, ItemStack inHand, String[] args) {
		// starting new book
		if (inHand.getAmount() == 1 && plugin.perms.canCreateBook(player)) {
			short bookId = plugin.getNextBookId();
			if (bookId == -1) {
				// error, quit
				return;
			}

			int titleStart = 0;
			
			// check for chat mode
			boolean chatMode = false;
			if (args[0].equalsIgnoreCase("-" + BookWorm.S_COMM_CHATMODE)) {
				titleStart = 1;
				chatMode = true;
			} else if (BookWorm.AUTO_CHAT_MODE) {
				chatMode = true;
			}
			if (chatMode) {
				plugin.chatModed.add(player.getName());					
			}
			
			// get title
			String title = "";
			for (int i = titleStart; i < args.length; i++) {
				title += args[i] + " ";
			}
			
			// setup book
			Book book = new Book(bookId, title.trim(), player.getName());
			plugin.books.put(bookId, book);
			inHand.setDurability(bookId);
			book.setBookMeta(inHand);
			player.setItemInHand(inHand);
			BookWorm.metricBookCount++;
			
			// send messages
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NEW_BOOK_CREATED.replace("%t", BookWorm.TEXT_COLOR_2 + book.getTitle()));
			if (chatMode) {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_CHATMODE_ON);	
			}
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);
		}		
	}
	
	private void help(Player player, String label) {
		String[] lines = BookWorm.S_COMM_HELP_TEXT.split("\n");
		for (String line : lines) {
			if (!line.equals("")) {
				player.sendMessage(BookWorm.TEXT_COLOR + line.replace("%c", label).replace("--", BookWorm.TEXT_COLOR_2 + "--"));
			}
		}		
	}
	
	private void read(Player player, Book book, String[] args) {
		if (args.length == 2 && args[1].matches("[0-9]+")) {
			book.read(player, Integer.parseInt(args[1])-1);
		} else {
			book.read(player, 0);
		}
	}
	
	private void title(Player player, Book book, String[] args) {
		if (plugin.perms.canModifyBook(player, book)) {
			String title = "";
			for (int i = 1; i < args.length; i++) {
				title += args[i] + " ";
			}
			BookModifyEvent evt = new BookModifyEvent(player, book, ModifyType.TITLE_CHANGE, title.trim());
			Bukkit.getPluginManager().callEvent(evt);
			if (!evt.isCancelled()) {
				book.setTitle(evt.getContent());
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_TITLE_DONE + BookWorm.TEXT_COLOR_2 + book.getTitle());
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);
			}
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);
		}
	}
	
	private void author(Player player, Book book, String[] args) {
		if (plugin.perms.canChangeBookAuthor(player, book)) {
			String author = "";
			for (int i = 1; i < args.length; i++) {
				author += args[i] + " ";
			}
			BookModifyEvent evt = new BookModifyEvent(player, book, ModifyType.DISPLAY_AUTHOR_CHANGE, author.trim());
			if (!evt.isCancelled()) {
				book.addHiddenData("Author", evt.getContent());
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_AUTHOR_DONE + BookWorm.TEXT_COLOR_2 + book.getDisplayAuthor());
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);
			}
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);
		}
	}
	
	private void undo(Player player, Book book) {
		if (plugin.perms.canModifyBook(player, book)) {
			boolean undone = book.undo();
			if (undone) {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_UNDO_DONE);
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_UNDO_FAIL);
			}
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);
		}
	}
	
	private void erase(Player player, Book book, String[] args) {
		if (plugin.perms.canModifyBook(player, book)) {
			String s = "";
			for (int i = 1; i < args.length; i++) {
				s += args[i] + " ";
			}
			book.erase(s.trim());
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_ERASE_DONE);
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);								
		}
	}
	
	private void replace(Player player, Book book, String[] args) {
		if (plugin.perms.canModifyBook(player, book)) {
			String s = "";
			for (int i = 1; i < args.length; i++) {
				s += args[i] + " ";
			}
			s = s.trim();
			boolean replaced = book.replace(s.trim());
			if (replaced) {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_REPLACE_DONE);
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_REPLACE_FAIL);
			}
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);							
		}
	}
	
	private void eraseAll(Player player, Book book) {
		if (plugin.perms.canModifyBook(player, book)) {
			book.eraseAll();
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_ERASEALL_DONE);
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_NO_PERMISSION);			
		}
	}
	
	private void chatMode(Player player, String[] args) {
		int mode = -1;
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("on")) {
				mode = 1;
			} else if (args[1].equalsIgnoreCase("off")) {
				mode = 0;
			}
		} else if (plugin.chatModed.contains(player.getName())) {
			mode = 0;
		} else {	
			mode = 1;
		}
		if (mode == 1) {
			plugin.chatModed.add(player.getName());
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_CHATMODE_ON);							
		} else if (mode == 0) {
			plugin.chatModed.remove(player.getName());
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_CHATMODE_OFF);							
		} else {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_INVALID);							
		}
	}
	
	private void get(Player player, String[] args) {
		// check for valid id
		if (!args[1].matches("^[0-9]{1,5}$")) {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_GET_FAIL);
			return;
		}
		
		// get book
		short id = Short.parseShort(args[1]);
		Book book = plugin.getBookById(id);
		if (book == null) {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_GET_FAIL);
			return;
		}
		
		// check perms
		if (!plugin.perms.canSpawnBook(player, book)) {
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_GET_FAIL);
			return;			
		}
		
		// give book
		ItemStack item = new ItemStack(Material.BOOK, 1, id);
		if (player.getItemInHand() == null || player.getItemInHand().getType() == Material.AIR) {
			// set in hand
			player.setItemInHand(item);
		} else {
			int slot = player.getInventory().firstEmpty();
			if (slot >= 0) {
				// add to inventory
				player.getInventory().addItem(new ItemStack(Material.BOOK, 1, id));
			} else {
				// drop in front
				Item i = player.getWorld().dropItem(player.getLocation(), item);
				i.setVelocity(player.getLocation().getDirection());
			}
		}
		player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_COMM_GET_DONE + BookWorm.TEXT_COLOR_2 + book.getTitle());
	}
}
