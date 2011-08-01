package com.nisovin.realrp.character;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;
import org.bukkitcontrib.BukkitContrib;

import com.nisovin.realrp.RealRP;

public class PlayerCharacter implements GameCharacter {

	private static HashMap<Player,PlayerCharacter> characters;
	
	private Configuration config;
	
	private Player player;
	private String firstName;
	private String lastName;
	private String prefixTitle;
	private String postfixTitle;
	private String subTitle;
	
	private int age;
	private Sex sex;
	private String description;
	private ArrayList<CharacterNote> notes;
	private HashMap<Player,CharacterNote> newNotes;
	
	private String chatName;
	private String emoteName;
	private String nameplate;
	
	public static PlayerCharacter get(Player player) {
		if (characters == null) {
			characters = new HashMap<Player,PlayerCharacter>();
		}
		
		PlayerCharacter character = characters.get(player);
		if (character == null) {
			character = new PlayerCharacter(player);
			characters.put(player, character);
		}
		
		return character;
	}
	
	public PlayerCharacter(Player player) {
		this.player = player;
		
		config = new Configuration(new File(RealRP.getPlugin().getDataFolder(), "players" + File.separator + player.getName().toLowerCase() + ".yml"));
		config.load();
		
		firstName = config.getString("first-name", player.getName());
		lastName = config.getString("last-name", "");
		prefixTitle = config.getString("prefix-title", "");
		postfixTitle = config.getString("postfix-title", "");
		subTitle = config.getString("sub-title", "");
		age = config.getInt("age", 25);
		String sx = config.getString("sex", "u");
		if (sx.equalsIgnoreCase("m")) {
			sex = Sex.Male;
		} else if (sx.equalsIgnoreCase("f")) {
			sex = Sex.Female;
		} else {
			sex = Sex.Unknown;
		}
		description = config.getString("description", "");
		
		notes = new ArrayList<CharacterNote>();
		Map<String,ConfigurationNode> noteNodes = config.getNodes("notes");
		for (String key : noteNodes.keySet()) {
			ConfigurationNode node = noteNodes.get(key);
			Long time = Long.parseLong(key);
			String by = node.getString("by");
			String text = node.getString("note");
			CharacterNote note = new CharacterNote(time, by, text);
			notes.add(note);
		}		
		
		newNotes = new HashMap<Player,CharacterNote>();
		
		chatName = firstName;
		emoteName = firstName;
		nameplate = firstName;
		
		setUpNames();
	}
	
	public void sendMessage(String message, String... replacements) {
		String[] msgs = message.split("\n");
		for (String msg : msgs) {
			player.sendMessage(msg);
		}
	}
	
	public void setUpNames() {
		player.setDisplayName(getChatName());
		BukkitContrib.getAppearanceManager().setGlobalTitle(player, getNameplate());
	}

	@Override
	public String getChatName() {
		return chatName;
	}

	@Override
	public String getEmoteName() {
		return emoteName;
	}

	@Override
	public String getNameplate() {
		return nameplate;
	}
	
	public Sex getSex() {
		return sex;
	}
	
	public void save() {
		config.setProperty("first-name", firstName);
		config.setProperty("last-name", lastName);
		config.setProperty("prefix-title", prefixTitle);
		config.setProperty("postfix-title", postfixTitle);
		config.setProperty("sub-title", subTitle);
		config.setProperty("age", age);
		if (sex == Sex.Male) {
			config.setProperty("sex", "m");
		} else if (sex == Sex.Female) {
			config.setProperty("sex", "f");
		} else {
			config.setProperty("sex", "u");
		}
		config.setProperty("description", description);
		
		for (CharacterNote note : notes) {
			note.store(config);
		}
	}
	
	public void startNote(Player by) {
		CharacterNote note = new CharacterNote(by.getName());
		newNotes.put(by, note);
	}
	
	public boolean addNoteText(Player by, String text) {
		CharacterNote note = newNotes.get(by);
		
		if (note == null) {
			return false;
		}
		
		note.addText(text);
		return true;
	}
	
	public boolean saveNote(Player by) {
		CharacterNote note = newNotes.get(by);
		
		if (note == null) {
			return false;
		}
		
		notes.add(note);
		newNotes.remove(note);
		save();
		return true;
	}
	
	public enum Sex {
		Male, Female, Unknown
	}
	
	public class CharacterNote implements Comparable<CharacterNote> {
		private Long time;
		private String by;
		private String note;
		
		public CharacterNote(String by) {
			this.time = System.currentTimeMillis();
			this.by = by;
			this.note = "";
		}
		
		public CharacterNote(Long time, String by, String note) {
			this.time = time;
			this.by = by;
			this.note = note;
		}
		
		public void addText(String text) {
			note += text.trim() + " ";
		}
		
		public void store(Configuration config) {
			config.setProperty("notes." + time + ".by", by);
			config.setProperty("notes." + time + ".note", note);
		}
		
		@Override
		public int compareTo(CharacterNote n) {
			return this.time.compareTo(n.time);
		}
	}
	
}
