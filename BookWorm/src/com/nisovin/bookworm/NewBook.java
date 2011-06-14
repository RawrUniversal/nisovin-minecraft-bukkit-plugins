package com.nisovin.bookworm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NewBook {

	private String title;
	private String author;
	private String contents;
	private Book tempBook;
	
	public NewBook(Player player, String[] title) {
		this.author = player.getName();
		this.title = "";
		for (int i = 0; i < title.length; i++) {
			this.title += title[i] + " ";
		}
		this.title = this.title.trim();
		this.contents = "";
	}
	
	public NewBook(String author, String title, String contents) {
		this.author = author;
		this.title = title;
		this.contents = contents;
	}
	
	public String write(String[] text) {
		String line = "";
		for (int i = 0; i < text.length; i++) {
			contents += " " + text[i];
			line += " " + text[i];
		}
		contents = contents.trim();
		tempBook = null;
		return line.trim();
	}
	
	public void read(Player player, int page) {
		if (tempBook == null) {
			tempBook = new Book(title, author, contents);
		}
		tempBook.read(player, page);
	}
	
	public boolean replace(String s) {
		if (!s.contains("->")) {
			return false;
		}
		String[] fromTo = s.split("->", 2);
		if (!s.contains(fromTo[0].trim())) {
			return false;
		}
		contents = contents.replace(fromTo[0].trim(), fromTo[1].trim());
		tempBook = null;
		return true;
	}
	
	public void delete(String s) {
		contents = contents.replace(s, "");
		tempBook = null;
	}
	
	public void erase() {
		contents = "";
		tempBook = null;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void save(Location location) {
		String fileName = title.replace(" ", "-") + "_" + author + "_" + System.currentTimeMillis();
		String locStr = location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
		try {
			// write book file
			PrintWriter writer = new PrintWriter(new FileWriter(new File(BookWorm.plugin.getDataFolder(), fileName + ".txt"), false));
			writer.println(title);
			writer.println(author);
			writer.println(contents);
			writer.close();
			
			// append entry to book list
			writer = new PrintWriter(new FileWriter(new File(BookWorm.plugin.getDataFolder(), "books.txt"), true));
			writer.println(locStr + ":" + fileName);
			writer.close();
			
			// add book to plugin
			Book book = new Book(fileName);
			BookWorm.plugin.books.put(locStr, book);
		} catch (IOException e) {
			Bukkit.getServer().getLogger().severe("BookWorm: Failed to save book: " + title + " " + author);
		}
	}
	
}
