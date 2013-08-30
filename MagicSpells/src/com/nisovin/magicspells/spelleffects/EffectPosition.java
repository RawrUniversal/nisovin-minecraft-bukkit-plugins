package com.nisovin.magicspells.spelleffects;

public enum EffectPosition {

	START_CAST(0),
	CASTER(1),
	TARGET(2),
	TRAIL(3),
	DISABLED(4),
	DELAYED(5),
	SPECIAL(6);
	
	private int id;
	private EffectPosition(int num) {
		this.id = num;
	}
	
	public int getId() {
		return id;
	}
	
}
