package com.nisovin.MagicSpells;

import org.bukkit.util.config.Configuration;

public abstract class CommandSpell extends Spell {

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