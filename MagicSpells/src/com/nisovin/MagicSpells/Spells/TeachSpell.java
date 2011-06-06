package com.nisovin.MagicSpells.Spells;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Spellbook;

public class TeachSpell extends CommandSpell {

	private static final String SPELL_NAME = "teach";

	private String strUsage;
	private String strNoTarget;
	private String strNoSpell;
	private String strCantTeach;
	private String strCantLearn;
	private String strAlreadyKnown;
	private String strCastTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new TeachSpell(config, spellName));
		}
	}
	
	public TeachSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strUsage = config.getString("spells." + spellName + ".str-usage", "Usage: /cast teach <target> <spell>");
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "No such player.");
		strNoSpell = config.getString("spells." + spellName + ".str-no-spell", "You do not know a spell by that name.");
		strCantTeach = config.getString("spells." + spellName + ".str-cant-teach", "You can't teach that spell.");
		strCantLearn = config.getString("spells." + spellName + ".str-cant-learn", "That person cannot learn that spell.");
		strAlreadyKnown = config.getString("spells." + spellName + ".str-already-known", "That person already knows that spell.");
		strCastTarget = config.getString("spells." + spellName + ".str-cast-target", "%a has taught you the %s spell.");
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length != 2) {
				// fail: missing args
				sendMessage(player, strUsage);
			} else {
				List<Player> players = MagicSpells.plugin.getServer().matchPlayer(args[0]);
				if (players.size() != 1) {
					// fail: no player match
					sendMessage(player, strNoTarget);
				} else {
					Spell spell = MagicSpells.spellNames.get(args[1]);
					if (spell == null) {
						// fail: no spell match
						sendMessage(player, strNoSpell);
					} else {
						Spellbook spellbook = MagicSpells.getSpellbook(player);
						if (spellbook == null || !spellbook.hasSpell(spell)) {
							// fail: player doesn't have spell
							sendMessage(player, strNoSpell);
						} else if (!spellbook.canTeach(spell)) {
							// fail: cannot teach
							sendMessage(player, strCantTeach);
						} else {
							// yay! can learn!
							Spellbook targetSpellbook = MagicSpells.getSpellbook(players.get(0));
							if (targetSpellbook == null || !targetSpellbook.canLearn(spell)) {
								// fail: no spellbook for some reason or can't learn the spell
								sendMessage(player, strCantLearn);
							} else if (targetSpellbook.hasSpell(spell)) {
								// fail: target already knows spell
								sendMessage(player, strAlreadyKnown);
							} else {
								targetSpellbook.addSpell(spell);
								targetSpellbook.save();
								sendMessage(players.get(0), formatMessage(strCastTarget, "%a", player.getName(), "%s", spell.getName(), "%t", players.get(0).getName()));
								sendMessage(player, formatMessage(strCastSelf, "%a", player.getName(), "%s", spell.getName(), "%t", players.get(0).getName()));
								setCooldown(player);
								removeReagents(player);
							}
						}
					}
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		if (args == null || args.length != 2) {
			// fail: missing args
			sender.sendMessage(strUsage);
		} else {
			List<Player> players = MagicSpells.plugin.getServer().matchPlayer(args[0]);
			if (players.size() != 1) {
				// fail: no player match
				sender.sendMessage(strNoTarget);
			} else {
				Spell spell = MagicSpells.spellNames.get(args[1]);
				if (spell == null) {
					// fail: no spell match
					sender.sendMessage(strNoSpell);
				} else {
					// yay! can learn!
					Spellbook targetSpellbook = MagicSpells.getSpellbook(players.get(0));
					if (targetSpellbook == null || !targetSpellbook.canLearn(spell)) {
						// fail: no spellbook for some reason or can't learn the spell
						sender.sendMessage(strCantLearn);
					} else if (targetSpellbook.hasSpell(spell)) {
						// fail: target already knows spell
						sender.sendMessage(strAlreadyKnown);
					} else {
						targetSpellbook.addSpell(spell);
						targetSpellbook.save();
						sendMessage(players.get(0), formatMessage(strCastTarget, "%a", "Console", "%s", spell.getName(), "%t", players.get(0).getName()));
						sender.sendMessage(formatMessage(strCastSelf, "%a", "Console", "%s", spell.getName(), "%t", players.get(0).getName()));
					}
				}
			}
		}
		return true;
	}

}