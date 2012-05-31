package com.nisovin.magicspells.spells;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;
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

	@Override
	public abstract String tabComplete(CommandSender sender, String partial);
	
	protected String tabCompletePlayerName(String partial) {
		ArrayList<String> matches = new ArrayList<String>();
		partial = partial.toLowerCase();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().toLowerCase().startsWith(partial)) {
				matches.add(p.getName());
			}
		}
		if (matches.size() == 1) {
			return matches.get(0);
		} else if (matches.size() == 0) {
			return null;
		} else {
			// TODO: show player matches
			return null;
		}
	}
	
}