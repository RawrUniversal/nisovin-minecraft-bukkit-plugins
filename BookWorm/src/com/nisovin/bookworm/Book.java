package com.nisovin.bookworm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Book {
	private static final int LINE_LENGTH = 55;
	private static final int PAGE_LENGTH = 6;
	private static final String INDENT = "    ";
	private static final String NEW_PARA = "::";
	
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
		String[] paras = text.split(NEW_PARA);
		for (int i = 0; i < paras.length; i++) {
			if (!paras[i].equals("")) {
				String para = INDENT + paras[i].trim();
				while (para.length() > LINE_LENGTH) {
					int end = para.substring(0, LINE_LENGTH).lastIndexOf(' ');
					contents.add(para.substring(0, end));
					para = para.substring(end+1);
				}
				if (para.length() > 0) {
					contents.add(para);
				}
			}
		}
		this.contents = contents.toArray(new String[]{});
		
		this.pages = (this.contents.length-1)/PAGE_LENGTH + 1;
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
	
	public void read(Player player, int page) {
		// page is 0-based
		
		if (!loaded) {
			load();
		}
		if (loaded) {
			page = page % pages;
			
			player.sendMessage(BookWorm.TEXT_COLOR + "--------------------------------------------------");
			if (title.length() + author.length() + 25 > LINE_LENGTH) {
				player.sendMessage(BookWorm.TEXT_COLOR + "Book: " + ChatColor.WHITE + title);
				player.sendMessage(INDENT + BookWorm.TEXT_COLOR + "by: " + ChatColor.WHITE + author + BookWorm.TEXT_COLOR + " (page " + (page+1) + "/" + pages + ")");
			} else {
				player.sendMessage(BookWorm.TEXT_COLOR + "Book: " + ChatColor.WHITE + title + BookWorm.TEXT_COLOR + " by: " + ChatColor.WHITE + author + BookWorm.TEXT_COLOR + " (page " + (page+1) + "/" + pages + ")");
			}
			player.sendMessage("   ");
			for (int i = 0; i < PAGE_LENGTH && page*PAGE_LENGTH + i < contents.length; i++) {
				player.sendMessage(contents[page*PAGE_LENGTH + i]);
			}
			player.sendMessage(BookWorm.TEXT_COLOR + "--------------------------------------------------");
		}
	}
		
	public void load() {
		try {
			Scanner scanner = new Scanner(new File(BookWorm.plugin.getDataFolder(), id + ".txt"));
			String title = scanner.nextLine();
			String author = scanner.nextLine();
			String text = "";
			while (scanner.hasNext()) {
				text += scanner.nextLine();
			}
			scanner.close();
			
			setup(title, author, text);
			
		} catch (FileNotFoundException e) {
			System.out.println("Failed to load book: " + id);
		}
	}
	
}
