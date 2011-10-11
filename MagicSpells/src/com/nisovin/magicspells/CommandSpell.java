package com.nisovin.magicspells;

import org.bukkit.command.CommandSender;

import com.nisovin.magicspells.util.MagicConfig;

public abstract class CommandSpell extends Spell {

	public CommandSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}
	
	public boolean canCastWithItem() {
		return false;
	}
	
	public boolean canCastByCommand() {
		return true;
	}
	
	@Override
	public abstract boolean castFromConsole(CommandSender sender, String[] args);

}