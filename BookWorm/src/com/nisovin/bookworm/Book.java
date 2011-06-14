package com.nisovin.bookworm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Book {
	
	private String id;
	private boolean loaded;
	private String title;
	private String author;
	private String rawText;
	private String[] contents;
	private int pages;
	
	public Book(String id) {
		this.id = id;
		this.loaded = false;
	}
	
	public Book(String title, String author, String text) {
		setup(title, author, text);
	}
	
	private void setup(String title, String author, String text) {
		this.title = title;
		this.author = author;
		this.rawText = text;
				
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
		this.loaded = true;
	}
	
	public String getId() {
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
	
	public String getContents() {
		if (!loaded) load();
		return rawText;
	}
	
	public boolean isLoaded() {
		return loaded;
	}
	
	public void read(Player player, int page) {
		// page is 0-based
		
		if (!loaded) {
			load();
		}
		if (loaded) {
			page = page % pages;
			
			player.sendMessage(BookWorm.TEXT_COLOR + "--------------------------------------------------");
			if (title.length() + author.length() + 25 > BookWorm.LINE_LENGTH) {
				player.sendMessage(BookWorm.TEXT_COLOR + "Book: " + ChatColor.WHITE + title);
				player.sendMessage(BookWorm.INDENT + BookWorm.TEXT_COLOR + "by: " + ChatColor.WHITE + author + BookWorm.TEXT_COLOR + " (page " + (page+1) + "/" + pages + ")");
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + "Book: " + ChatColor.WHITE + title + BookWorm.TEXT_COLOR + " by: " + ChatColor.WHITE + author + BookWorm.TEXT_COLOR + " (page " + (page+1) + "/" + pages + ")");
			}
			player.sendMessage("   ");
			for (int i = 0; i < BookWorm.PAGE_LENGTH && page*BookWorm.PAGE_LENGTH + i < contents.length; i++) {
				player.sendMessage(contents[page*BookWorm.PAGE_LENGTH + i]);
			}
			player.sendMessage(BookWorm.TEXT_COLOR + "--------------------------------------------------");
		}
	}
		
	public void load() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(BookWorm.plugin.getDataFolder(), id + ".txt")));
			String title = reader.readLine();
			String author = reader.readLine();
			String text = "";
			String line;
			while ((line = reader.readLine()) != null) {
				text += line;
			}
			reader.close();
			
			setup(title, author, text);
			
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load book (file not found): " + id);
		} catch (IOException e) {
			System.out.println("Failed to load book: " + id);
		}
	}
	
	public void unload() {
		title = null;
		author = null;
		rawText = null;
		contents = null;
		loaded = false;
	}
	
}
