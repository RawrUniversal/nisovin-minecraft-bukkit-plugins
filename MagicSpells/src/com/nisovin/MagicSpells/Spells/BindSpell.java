package com.nisovin.MagicSpells.Spells;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Spellbook;

public class BindSpell extends CommandSpell {

	private static final String SPELL_NAME = "bind";
	
	private String strUsage;
	private String strNoSpell;
	private String strCantBind;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new BindSpell(config, spellName));
		}
	}

	public BindSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strUsage = config.getString("spells." + spellName + ".str-usage", "You must specify a spell name and hold an item in your hand.");
		strNoSpell = config.getString("spells." + spellName + ".str-no-spell", "You do not know a spell by that name.");
		strCantBind = config.getString("spells." + spellName + ".str-cant-bind", "That spell cannot be bound to an item.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length != 1 || player.getItemInHand().getTypeId() <= 0) {
				sendMessage(player, strUsage);
			} else {
				Spell spell = MagicSpells.spellNames.get(args[0]);
				Spellbook spellbook = MagicSpells.getSpellbook(player);
				if (spell == null || spellbook == null) {
					// fail - no such spell, or no spellbook
					sendMessage(player, strNoSpell);
				} else if (!spellbook.hasSpell(spell)) {
					// fail - doesn't know spell
					sendMessage(player, strNoSpell);
				} else if (!spell.canCastWithItem()) {
					// fail - spell can't be bound
					sendMessage(player, strCantBind);
				} else {
					spellbook.removeSpell(spell);
					spellbook.addSpell(spell, player.getItemInHand().getTypeId());
					spellbook.save();
					removeReagents(player);
					setCooldown(player);
					sendMessage(player, formatMessage(strCastSelf, "%s", spell.getName()));
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
