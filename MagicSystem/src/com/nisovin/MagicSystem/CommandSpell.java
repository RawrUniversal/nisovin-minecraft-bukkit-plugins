package com.nisovin.MagicSystem;

public class CommandSpell extends Spell {

	public CommandSpell(Configuration config, String spellName) {
		super(config, spellName);
	}
	
	public boolean canCastWithItem() {
		return false;
	}
	
	public boolean canCastByCommand() {
		return true;
	}

}