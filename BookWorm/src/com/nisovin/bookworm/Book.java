package com.nisovin.bookworm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Book {
	
	private short id;
	private boolean loaded;
	private boolean unsaved;
	private String title;
	private String author;
	private String text;
	private String[] contents;
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
	}
	
	private void generateContents() {
		ArrayList<String> contents = new ArrayList<String>();
		String[] paras = text.split(BookWorm.NEW_PARA);
		for (int i = 0; i < paras.length; i++) {
			if (!paras[i].equals("")) {
				String para = BookWorm.INDENT + paras[i].trim();
				while (para.length() > BookWorm.LINE_LENGTH) {
					int end = para.substring(0, BookWorm.LINE_LENGTH).lastIndexOf(' ');
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
	
	public short getId() {
		return id;
	}
	
	public String getAuthor() {
		if (!loaded) load();
		return author;
	}
	
	public String getTitle() {
		if (!loaded) load();
		return title;
	}
	
	public void setTitle(String title) {
		// delete old file
		File f = getFile();
		if (f != null && f.exists()) {
			f.delete();
		}
		this.title = title;
		unsaved = true;
	}
	
	public String getContents() {
		if (!loaded) load();
		return text;
	}
	
	public boolean hasHiddenData(String key) {
		return hiddenData.containsKey(key);
	}
	
	public void addHiddenData(String key, String value) {
		hiddenData.put(key, value);
		unsaved = true;
	}
	
	public String getHiddenData(String key) {
		return hiddenData.get(key);
	}
	
	public String removeHiddenData(String key) {
		if (hiddenData.containsKey(key)) {
			unsaved = true;
			return hiddenData.remove(key);
		} else {
			return null;
		}
	}
	
	public String write(String[] text) {
		String line = "";
		for (int i = 0; i < text.length; i++) {
			this.text += " " + text[i];
			line += " " + text[i];
		}
		this.text = this.text.trim();
		unsaved = true;
		return line.trim();
	}
	
	public boolean replace(String s) {
		if (!s.contains("->")) {
			return false;
		}
		String[] fromTo = s.split("->", 2);
		if (!s.contains(fromTo[0].trim())) {
			return false;
		}
		text = text.replace(fromTo[0].trim(), fromTo[1].trim());
		unsaved = true;
		return true;
	}
	
	public void erase(String s) {
		text = text.replace(s, "");
		unsaved = true;
	}
	
	public void eraseAll() {
		text = "";
		unsaved = true;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public boolean isSaved() {
		return !unsaved;
	}
	
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
			
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_DIVIDER);
			if (title.length() + author.length() + 25 > BookWorm.LINE_LENGTH) {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + title);
				player.sendMessage(BookWorm.INDENT + BookWorm.TEXT_COLOR + BookWorm.S_READ_BY + ": " + BookWorm.TEXT_COLOR_2 + author + BookWorm.TEXT_COLOR + " (" + BookWorm.S_READ_PAGE + " " + (page+1) + "/" + pages + ")");
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_BOOK + ": " + BookWorm.TEXT_COLOR_2 + title + BookWorm.TEXT_COLOR + " " + BookWorm.S_READ_BY + ": " + BookWorm.TEXT_COLOR_2 + author + BookWorm.TEXT_COLOR + " (" + BookWorm.S_READ_PAGE + " " + (page+1) + "/" + pages + ")");
			}
			player.sendMessage("   ");
			for (int i = 0; i < BookWorm.PAGE_LENGTH && page*BookWorm.PAGE_LENGTH + i < contents.length; i++) {
				player.sendMessage(contents[page*BookWorm.PAGE_LENGTH + i]);
			}
			player.sendMessage(BookWorm.TEXT_COLOR + BookWorm.S_READ_DIVIDER);
		}
	}
	
	public void save() {
		generateContents();
		
		String fileName;
		if (BookWorm.USE_FULL_FILENAMES) {
			String t = title.replace(" ", "-").replaceAll("[^a-zA-Z0-9_\\-]", "");
			if (t.length() > 15) {
				t = t.substring(0, 15);
			}
			fileName = id + "_" + author + "_" + t; // TODO: what if they change the book title?
		} else {
			fileName = id+"";
		}
		
		try {
			// write book file
			PrintWriter writer = new PrintWriter(new FileWriter(new File(BookWorm.plugin.getDataFolder(), fileName + ".txt"), false));
			writer.println(id);
			writer.println(title);
			writer.println(author);
			for (String s : hiddenData.keySet()) {
				writer.println("|!|" + s + "|" + hiddenData.get(s));
			}
			writer.println(text);
			writer.close();
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("BookWorm: Failed to save book: " + title + " " + author);
		}
		unsaved = false;
	}
		
	public boolean load() {
		try {
			// get correct file
			File bookFile = getFile();
			
			BufferedReader reader = new BufferedReader(new FileReader(bookFile));
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
	
	private File getFile() {
		File bookDir = BookWorm.plugin.getDataFolder();
		File bookFile = new File(bookDir, id + ".txt");
		if (!bookFile.exists()) {
			for (File file : bookDir.listFiles()) {
				if (file.getName().startsWith(id+"_")) {
					bookFile = file;
					break;
				}
			}
		}
		return bookFile;
	}
	
	public void unload() {
		title = null;
		author = null;
		text = null;
		contents = null;
		loaded = false;
	}
	
}
