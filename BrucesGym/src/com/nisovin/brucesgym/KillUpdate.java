package com.nisovin.brucesgym;

public class KillUpdate implements DatabaseUpdate {

	String killerName;
	String killedName;
	GymGameMode gameMode;
	String weapon;
	
	public KillUpdate(String killerName, String killedName, GymGameMode gameMode, String weapon) {
		this.killerName = killerName;
		this.killedName = killedName;
		this.gameMode = gameMode;
		this.weapon = weapon;
	}
	
}
