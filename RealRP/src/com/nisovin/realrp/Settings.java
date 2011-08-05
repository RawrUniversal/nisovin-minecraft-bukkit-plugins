package com.nisovin.realrp;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.bukkit.util.config.Configuration;

public class Settings {

	// Character Creation Section
	
	public boolean ccEnableCharacterCreator = true;
	
	public String ccFirstNameGet = "Please enter your character's first name.";
	public String ccFirstNameRegex = "^[A-Z][a-z][A-Za-z]+$";
	public String ccFirstNameOk = "Your first name is: %v";
	public String ccFirstNameInvalid = "Invalid first name.";
	
	public String ccLastNameGet = "Please enter your character's last name.";
	public String ccLastNameRegex = "^[A-Z][a-z][A-Za-z]+$";
	public String ccLastNameOk = "Your last name is: %v";
	public String ccLastNameInvalid = "Invalid last name.";
	
	public String ccAgeGet = "Please enter your character's age.";
	public String ccAgeRegex = "^[1-9][0-9]?$";
	public String ccAgeOk = "Your age is: %v";
	public String ccAgeInvalid = "Invalid age.";
	
	public String ccSexGet = "Please enter your character's sex.";
	public String ccSexRegex = "^[MFWBGmfwbg][A-za-z]*$";
	public String ccSexOk = "Your sex is: %v";
	public String ccSexInvalid = "Invalid sex.";
	
	public String ccDescriptionGet = "Please enter a short description for your character.";
	public String ccDescriptionRegex = "^.*$";
	public String ccDescriptionOk = "Description ok.";
	public String ccDescriptionInvalid = "Description invalid.";
	
	// Emote System Section
	
	public boolean emEnableEmotes = true;
	public String emEmotePrefix = "&e* ";
	public int emEmoteRange = 20;
	
	// Chat System Section
	
	public boolean csEnableChatSystem = true;
	
	public String csICName = "ic";
	public String csICPrefix = ".";
	public String csICFormat = "&e%n says: &f%m";
	public int csICRange = 20;
	
	public boolean csLocalOOCEnabled = true;
	public String csLocalOOCName = "ooc";
	public String csLocalOOCPrefix = "((";
	public String csLocalOOCFormat = "[&bOOC&f] <%n> %m";
	public int csLocalOOCRange = 50;
	
	public boolean csGlobalOOCEnabled = true;
	public String csGlobalOOCName = "global";
	public String csGlobalOOCPrefix = "!!";
	public String csGlobalOOCFormat = "[&3Global&f] <%n> %m";
	
	public boolean csIRCEnabled = false;
	public String csIRCNetwork = "irc.esper.net";
	public String csIRCNickname = "RPBot" + (int)(Math.random()*1000);
	public String csIRCNickservPass = "";
	public String csIRCChannel = "#realrp";
	public String csIRCFormatToIRC = "[Game] <%n> %m";
	public String csIRCFormatToGame = "[&aIRC&f] <%n> %m";
	
	
	
	public Settings(RealRP plugin) {
		// make sure folder exists
		File folder = plugin.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		// load config file
		File file = new File(plugin.getDataFolder(), "config.yml");
		boolean exists = file.exists();		
		Configuration config = new Configuration(file);
		config.load();
		
		// setup config categories
		HashMap<String,String> prefixes = new HashMap<String,String>();
		prefixes.put("cc", "CharCreator");
		prefixes.put("em", "EmoteSystem");
		prefixes.put("cs", "ChatSystem");
		
		// load config automatically based on above field names (wow, I'm lazy)
		Field[] fields = getClass().getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			String section = prefixes.get(name.substring(0, 2));
			name = name.substring(2);
			if (field.getType() == boolean.class) {
				try {
					field.set(this, config.getBoolean(section + "." + name, field.getBoolean(this)));
				} catch (IllegalAccessException e) {					
				}
			} else if (field.getType() == String.class) {
				try {
					field.set(this, config.getString(section + "." + name, (String)field.get(this)));
				} catch (IllegalAccessException e) {					
				}				
			} else if (field.getType() == int.class) {
				try {
					field.set(this, config.getInt(section + "." + name, field.getInt(this)));
				} catch (IllegalAccessException e) {					
				}				
			}
		}
		
		if (!exists) {
			config.save();
		}
		
	}
	
}
