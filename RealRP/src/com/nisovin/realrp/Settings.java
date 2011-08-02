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
	
}
