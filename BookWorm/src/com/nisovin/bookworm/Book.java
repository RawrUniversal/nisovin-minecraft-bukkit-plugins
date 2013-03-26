package com.nisovin.bookworm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Book {
	
	private short id;
	private boolean loaded;
	private boolean unsaved;
	private String title;
	private String author;
	private String text;
	private String[] contents;
	private String lastText;
	private HashMap<String,String> hiddenData;
	private int pages;
	
	protected Book(short id) {
		this.id = id;
		this.loaded = false;
		this.unsaved = false;
		hiddenData = new HashMap<String,String>();
	}
	
	protected Book(short id, String title, String author) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.text = "";
		this.loaded = true;
		this.unsaved = true;
		hiddenData = new HashMap<String,String>();
		setItemName();
	}
	
	protected Book(short id, Book book) {
		this.id = id;
		this.title = book.title;
		this.author = book.author;
		this.text = book.text;
		this.contents = book.contents.clone();
		this.lastText = "";
		this.hiddenData = new HashMap<String,String>();
		this.loaded = true;
		this.unsaved = true;
		setItemName();
		save();
	}
	
	private void generateContents() {
		ArrayList<String> contents = new ArrayList<String>();
		String[] paras = text.split(BookWorm.NEW_PARA);
		for (int i = 0; i < paras.length; i++) {
			if (!paras[i].equals("")) {
				String para = BookWorm.INDENT + paras[i].trim();
				if (BookWorm.ENABLE_COLORS) {
					para = BookWorm.colorize(para);
				}
				while (para.length() > BookWorm.LINE_LENGTH) {
					int end = para.substring(0, BookWorm.LINE_LENGTH).lastIndexOf(' ');
                    if (end < 0) {
                        end = BookWorm.LINE_LENGTH;
                    }
					contents.add(para.substring(0, end));
					para = para.substring(end+1);
				}
				if (para.length() > 0) {
					contents.add(para);
				}
			}
		}
		this.contents = contents.toArray(new String[]{});
		
		this.pages = (this.contents.length-1)/BookWorm.PAGE_LENGTH + 1;
	}
	
	/**
	 * Gets the id number of this book.
	 * @return the id number
	 */
	public short getId() {
		return id;
	}
	
	/**
	 * Gets the author of this book.
	 * @return the author
	 */
	public String getAuthor() {
		if (!loaded) load();
		return author;
	}
	
	/**
	 * Gets the displayed author of this book.
	 * @return the displayed author
	 */
	public String getDisplayAuthor() {
		if (!loaded) load();
		String auth = getHiddenData("Author");
		if (auth == null) {
			auth = author;
		}
		if (BookWorm.ENABLE_COLORS) {
			auth = BookWorm.colorize(auth);
		}
		return auth;
	}
	
	/**
	 * Gets the title of this book.
	 * @return the title
	 */
	public String getTitle() {
		if (!loaded) load();
		if (BookWorm.ENABLE_COLORS) {
			return BookWorm.colorize(title);
		} else {
			return title;
		}
	}
	
	/**
	 * Sets the title of this book.
	 * @param title the new title
	 */
	public void setTitle(String title) {
		// delete old file
		File f = getFile();
		if (f != null && f.exists()) {
			f.delete();
		}
		this.title = title;
		unsaved = true;
		setItemName();
	}
	
	public void setBookMeta(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (!meta.hasDisplayName()) {
			meta.setDisplayName(ChatColor.RESET.toString() + BookWorm.TEXT_COLOR + getTitle());
			if (!meta.hasLore()) {
				List<String> lore = new ArrayList<String>();
				lore.add(ChatColor.RESET + "    " + BookWorm.TEXT_COLOR_2 + BookWorm.S_READ_BY + ": " + getAuthor());
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
		}
	}
	
	private void setItemName() {
		// TODO
	}
	
	/**
	 * Gets the full contents of this book as a single string.
	 * @return the contents
	 */
	public String getContents() {
		if (!loaded) load();
		return text;
	}
	
	/**
	 * Checks if this book has hidden data defined with the specified key.
	 * @param key the key to check
	 * @return whether the data exists
	 */
	public boolean hasHiddenData(String key) {
		return hiddenData.containsKey(key);
	}
	
	/**
	 * Adds hidden data to this book.
	 * @param key the key of the data
	 * @param value the actual data
	 */
	public void addHiddenData(String key, String value) {
		hiddenData.put(key, value);
		unsaved = true;
	}
	
	/**
	 * Gets the hidden data for the specified key.
	 * @param key the data key
	 * @return the hidden data, or null if there is no data for that key
	 */
	public String getHiddenData(String key) {
		return hiddenData.get(key);
	}
	
	/**
	 * Removes the hidden data for the specified key.
	 * @param key the data key
	 * @return the hidden data that was removed, or null if there wasn't any data
	 */
	public String removeHiddenData(String key) {
		if (hiddenData.containsKey(key)) {
			unsaved = true;
			return hiddenData.remove(key);
		} else {
			return null;
		}
	}
	
	/**
	 * Writes text into this book.
	 * @param text the text to write
	 * @return the text that was written
	 */
	public String write(String text) {
		BookWorm.metricWrites++;
		lastText = this.text;
		this.text += " " + text.trim();
		this.text = this.text.trim();
		unsaved = true;
		if (BookWorm.ENABLE_COLORS) {
			return BookWorm.colorize(text.trim());
		} else {
			return text.trim();
		}
	}

	/**
	 * Writes text into this book.
	 * @param text the text to write
	 * @return the text that was written
	 */
	public String write(String[] text) {
		BookWorm.metricWrites++;
		lastText = this.text;
		String line = "";
		for (int i = 0; i < text.length; i++) {
			this.text += " " + text[i];
			line += " " + text[i];
		}
		this.text = this.text.trim();
		unsaved = true;
		if (BookWorm.ENABLE_COLORS) {
			return BookWorm.colorize(line.trim());
		} else {
			return line.trim();
		}
	}
	
	/**
	 * Replaces text in the book with other text.
	 * @param s the replacement, in the format: old text -> new text
	 * @return whether text was replaced
	 */
	public boolean replace(String s) {
		if (!s.contains("->")) {
			return false;
		}
		String[] fromTo = s.split("->", 2);
		if (!s.contains(fromTo[0].trim())) {
			return false;
		}
		lastText = text;
		text = text.replace(fromTo[0].trim(), fromTo[1].trim());
		unsaved = true;
		return true;
	}
	
	/**
	 * Removes the specified text from the contents of this book.
	 * @param s the text to erase
	 */
	public void erase(String s) {
		lastText = text;
		text = text.replace(s, "");
		unsaved = true;
	}
	
	/**
	 * Erases all of the content of this book.
	 */
	public void eraseAll() {
		lastText = text;
		text = "";
		unsaved = true;
	}
	
	/**
	 * Undoes the last modification to this book.
	 * There is only one undo level.
	 * @return whether the undo was successful
	 */
	public boolean undo() {
		if (lastText != null && !lastText.equals("")) {
			text = lastText;
			lastText = "";
			unsaved = true;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Checks whether the contents of this book have been loaded.
	 * @return whether the contents are loaded
	 */
	public boolean isLoaded() {
		return loaded;
	}
	
	/**
	 * Checks whether this book has unsaved changes.
	 * @return true if there are no unsaved changes, false otherwise
	 */
	public boolean isSaved() {
		return !unsaved;
	}
	
	/**
	 * Sends the contents of a specified page to a player.
	 * @param player the player to send the page to
	 * @param page the page number
	 */
	public void read(Player player, int page) {
		// page is 0-based
		
		if (!loaded) {
			load();
		}
		if (unsaved) {
			save();
		}
		if (loaded) {
			page = page % pages;
			
			String dispAuthor = getDisplayAuthor();
			String title = getTitle();
			
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_DIVIDER);
			if (title.length() + dispAuthor.length() + 25 > BookWorm.LINE_LENGTH && dispAuthor.length() > 0) {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + title);
				player.sendMessage(BookWorm.INDENT + BookWorm.TEXT_COLOR + BookWorm.S_READ_BY + ": " + BookWorm.TEXT_COLOR_2 + dispAuthor + BookWorm.TEXT_COLOR + " (" + BookWorm.S_READ_PAGE + " " + (page+1) + "/" + pages + ")");
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + title + BookWorm.TEXT_COLOR + (dispAuthor.length() > 0 ? " " + BookWorm.S_READ_BY + ": " + BookWorm.TEXT_COLOR_2 + dispAuthor + BookWorm.TEXT_COLOR : "") + " (" + BookWorm.S_READ_PAGE + " " + (page+1) + "/" + pages + ")");
			}
			player.sendMessage("   ");
			for (int i = 0; i < BookWorm.PAGE_LENGTH && page*BookWorm.PAGE_LENGTH + i < contents.length; i++) {
				player.sendMessage(contents[page*BookWorm.PAGE_LENGTH + i]);
			}
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_DIVIDER);
		}
	}
	
	/**
	 * Gets the contents of a specific page.
	 * @param page the page number
	 * @return the lines on that page
	 */
	public String[] getPage(int page) {
		if (!loaded) {
			load();
		}
		if (unsaved) {
			save();
		}
		
		if (!loaded) {
			return null;
		}
		
		if (page < 0 || page > pages) {
			return null;
		}
		
		String[] lines = new String[BookWorm.PAGE_LENGTH];
		for (int i = 0; i < BookWorm.PAGE_LENGTH && page*BookWorm.PAGE_LENGTH + i < contents.length; i++) {
			lines[i] = contents[page*BookWorm.PAGE_LENGTH + i];
		}
		
		return lines;
	}
	
	/**
	 * Gets the number of pages in this book.
	 * @return the number of pages
	 */
	public int pageCount() {
		return pages;
	}
	
	/**
	 * Saves the book to disk.
	 */
	public void save() {
		generateContents();
		
		String fileName;
		if (BookWorm.USE_FULL_FILENAMES) {
			String t = title.replace(" ", "-").replaceAll("[^a-zA-Z0-9_\\-]", "");
			if (t.length() > 15) {
				t = t.substring(0, 15);
			}
			fileName = id + "_" + author + "_" + t;
		} else {
			fileName = id+"";
		}
		
		try {
			// write book file
			File file = new File(BookWorm.plugin.getDataFolder(), fileName + ".txt");
			if (file.exists()) {
				file.delete();
			}
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), BookWorm.CHARSET));
			writer.write(id+"");
			writer.newLine();
			writer.write(title);
			writer.newLine();
			writer.write(author);
			writer.newLine();
			for (String s : hiddenData.keySet()) {
				writer.write("|!|" + s + "|" + hiddenData.get(s));
				writer.newLine();
			}
			writer.write(text);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("BookWorm: Failed to save book: " + title + " " + author);
		}
		unsaved = false;
	}
	
	/**
	 * Loads the book from disk.
	 * @return whether it was loaded successfully
	 */
	public boolean load() {
		try {
			// get correct file
			File bookFile = getFile();
			if (bookFile == null || !bookFile.exists()) {
				System.out.println("Failed to load book (file not found): " + id);
				return false;
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bookFile), BookWorm.CHARSET));
			reader.readLine();
			String title = reader.readLine();
			String author = reader.readLine();
			String text = "";
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("|!|")) {
					String[] data = line.substring(3).split("\\|", 2);
					hiddenData.put(data[0], data[1]);
				} else {
					text += line;
				}
			}
			reader.close();
			
			this.title = title;
			this.author = author;
			this.text = text;
			this.loaded = true;
			this.unsaved = false;
			setItemName();
			generateContents();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load book (file not found): " + id);
			return false;
		} catch (IOException e) {
			System.out.println("Failed to load book: " + id);
			return false;
		}
	}
	
	/**
	 * Deletes this book, and opens its id number for a new book.
	 * @return whether the book was deleted successfully
	 */
	public boolean delete() {
		try {
			unload();
			File file = getFile();
			File dir = new File(BookWorm.plugin.getDataFolder(), "deleted");
			if (!dir.exists()) dir.mkdir();
			File moved = new File(dir, file.getName());
			int c = 0;
			while (moved.exists()) {
				moved = new File(dir, file.getName() + c++);
			}
			return file.renameTo(moved);
		} catch (Exception e) {
			return false;
		}
	}
	
	private File getFile() {
		File bookDir = BookWorm.plugin.getDataFolder();
		File bookFile = new File(bookDir, id + ".txt");
		if (!bookFile.exists()) {
			bookFile = null;
			for (File file : bookDir.listFiles()) {
				if (file.getName().startsWith(id+"_")) {
					bookFile = file;
					break;
				}
			}
		}
		return bookFile;
	}
	
	/**
	 * Unloads this book from memory.
	 */
	public void unload() {
		title = null;
		author = null;
		text = null;
		contents = null;
		loaded = false;
	}
	
}
