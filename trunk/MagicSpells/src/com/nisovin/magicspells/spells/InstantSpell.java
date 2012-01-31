package com.nisovin.magicspells.spells;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.MagicConfig;

public abstract class InstantSpell extends Spell {
	
	private boolean castWithItem;
	private boolean castByCommand;
	
	public InstantSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		castWithItem = config.getBoolean("spells." + spellName + ".can-cast-with-item", true);
		castByCommand = config.getBoolean("spells." + spellName + ".can-cast-by-command", true);
	}
	
	public boolean canCastWithItem() {
		return castWithItem;
	}
	
	public boolean canCastByCommand() {
		return castByCommand;
	}
}
