package com.nisovin.realrp;

public class Settings {

	public boolean enableCharacterCreator = true;
	
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
	
	
	public boolean enableEmotes = true;
	public String emotePrefix = "&e* ";
	
	
	public boolean enableChatSystem = true;
	
	public String csICName = "ic";
	public String csICPrefix = ".";
	public String csICFormat = "&e%1$s says: &f%2$s";
	public int csICRange = 20;
	
	public boolean csLocalOOCEnabled = true;
	public String csLocalOOCName = "ooc";
	public String csLocalOOCPrefix = "((";
	public String csLocalOOCFormat = "[&bOOC&f] <%1$s> %2$s";
	public int csLocalOOCRange = 50;
	
	public boolean csGlobalOOCEnabled = true;
	public String csGlobalOOCName = "global";
	public String csGlobalOOCPrefix = "!!";
	public String csGlobalOOCFormat = "[&3Global&f] <%1$s> %2$s";
	
	public boolean csIRCEnabled = false;
	public String csIRCNetwork = "irc.esper.net";
	public String csIRCNickname = "RPBot" + (int)(Math.random()*1000);
	public String csIRCChannel = "#realrp";
	
}
