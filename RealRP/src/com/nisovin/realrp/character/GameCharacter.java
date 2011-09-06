package com.nisovin.realrp.character;

public interface GameCharacter {

	public String getChatName();
	
	public String getEmoteName();
	
	public String getNameplate();
	
	public Sex getSex();
	
	public enum Sex {
		Male, Female, Unknown
	}
	
}
