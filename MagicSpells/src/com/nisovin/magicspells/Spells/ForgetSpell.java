package com.nisovin.MagicSpells.Spells;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Spellbook;

public class ForgetSpell extends CommandSpell {

	private String strUsage;
	private String strNoTarget;
	private String strNoSpell;
	private String strDoesntKnow;
	private String strCastTarget;
	
	public ForgetSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strUsage = config.getString("spells." + spellName + ".str-usage", "Usage: /cast forget <target> <spell>");
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "No such player.");
		strNoSpell = config.getString("spells." + spellName + ".str-no-spell", "You do not know a spell by that name.");
		strDoesntKnow = config.getString("spells." + spellName + ".str-doesnt-know", "That person does not know that spell.");
		strCastTarget = config.getString("spells." + spellName + ".str-cast-target", "%a has made you forget the %s spell.");
	}
	
	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
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
					boolean all = false;
					Spell spell = null;
					if (args[1].equals("*")) {
						all = true;
					} else {
						spell = MagicSpells.getSpellByInGameName(args[1]);
					}
					if (spell == null && !all) {
						// fail: no spell match
						sendMessage(player, strNoSpell);
					} else {
						Spellbook spellbook = MagicSpells.getSpellbook(player);
						if (spellbook == null) {
							// fail: missing spellbook
							sendMessage(player, strNoSpell);							
						} else if (!all && !spellbook.hasSpell(spell)) {
							// fail: player doesn't have spell
							sendMessage(player, strNoSpell);
						} else if (all && !spellbook.hasAdvancedPerm()) {
							// fail: caster wants to remove all spells but doesn't have advanced perms
							sendMessage(player, strNoSpell);
						} else {
							// yay! can forget!
							Spellbook targetSpellbook = MagicSpells.getSpellbook(players.get(0));
							if (targetSpellbook == null) {
								// fail: missing spellbook
								sendMessage(player, strDoesntKnow);
							} else if (!all && !targetSpellbook.hasSpell(spell)) {
								// fail: no spellbook for some reason or can't learn the spell
								sendMessage(player, strDoesntKnow);
							} else if (!all) {
								targetSpellbook.removeSpell(spell);
								targetSpellbook.save();
								sendMessage(players.get(0), formatMessage(strCastTarget, "%a", player.getDisplayName(), "%s", spell.getName(), "%t", players.get(0).getDisplayName()));
								sendMessage(player, formatMessage(strCastSelf, "%a", player.getDisplayName(), "%s", spell.getName(), "%t", players.get(0).getDisplayName()));
								return PostCastAction.NO_MESSAGES;
							} else if (all) {
								targetSpellbook.removeAllSpells();
								targetSpellbook.addGrantedSpells();
								targetSpellbook.save();
								sendMessage(player, "You have reset " + players.get(0).getName() + "'s spellbook.");
								return PostCastAction.NO_MESSAGES;
							}
						}
					}
				}
			}
			return PostCastAction.ALREADY_HANDLED;
		}
		return PostCastAction.HANDLE_NORMALLY;
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
				Spell spell = MagicSpells.getSpellByInGameName(args[1]);
				if (spell == null) {
					// fail: no spell match
					sender.sendMessage(strNoSpell);
				} else {
					Spellbook targetSpellbook = MagicSpells.getSpellbook(players.get(0));
					if (targetSpellbook == null || !targetSpellbook.hasSpell(spell)) {
						// fail: no spellbook for some reason or can't learn the spell
						sender.sendMessage(strDoesntKnow);
					} else {
						targetSpellbook.removeSpell(spell);
						targetSpellbook.save();
						sendMessage(players.get(0), formatMessage(strCastTarget, "%a", MagicSpells.strConsoleName, "%s", spell.getName(), "%t", players.get(0).getDisplayName()));
						sender.sendMessage(formatMessage(strCastSelf, "%a", MagicSpells.strConsoleName, "%s", spell.getName(), "%t", players.get(0).getDisplayName()));
					}
				}
			}
		}
		return true;
	}

}