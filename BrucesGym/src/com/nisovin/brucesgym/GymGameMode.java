package com.nisovin.brucesgym;

public enum GymGameMode {

	GLOBAL(0, "gl"),
	DWARVES_VS_ZOMBIES(1, "dvz"),
	GOLDEN_MONOCLE(2, "gm"),
	BARN_YARD_BLITZ(3, "byb"),
	DUNGEON_HUNTERS(4, "dh");
	
	private int id;
	private String code;
	
	GymGameMode(int id, String code) {
		this.id = id;
		this.code = code;
	}
	
	public int getId() {
		return id;
	}
	
	public String getCode() {
		return code;
	}
	
}
