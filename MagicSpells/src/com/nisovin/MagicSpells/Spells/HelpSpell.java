package com.nisovin.MagicSpells.Spells;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Spellbook;

public class HelpSpell extends CommandSpell {
	
	private static final String SPELL_NAME = "help";
	
	private String strUsage;
	private String strNoSpell;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new HelpSpell(config, spellName));
		}
	}

	public HelpSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strUsage = config.getString("spells." + spellName + ".str-usage", "Usage: /cast " + name + " <spell>");
		strNoSpell = config.getString("spells." + spellName + ".str-no-spell", "You do not know a spell by that name.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				sendMessage(player, strUsage);
			} else {
				Spell spell = MagicSpells.spellNames.get(args[0]);
				Spellbook spellbook = MagicSpells.spellbooks.get(player.getName());
				if (spell == null || spellbook == null || !spellbook.hasSpell(spell)) {
					sendMessage(player, strNoSpell);
				} else {
					sendMessage(player, spell.getName() + " - " + spell.getDescription());
					if (spell.getCostStr() != null && !spell.getCostStr().equals("")) {
						sendMessage(player, "Cost: " + spell.getCostStr());
					}
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

}
