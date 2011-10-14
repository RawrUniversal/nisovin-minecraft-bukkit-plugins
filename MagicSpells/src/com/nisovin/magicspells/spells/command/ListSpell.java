package com.nisovin.magicspells.spells.command;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.CommandSpell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.util.MagicConfig;

public class ListSpell extends CommandSpell {
	
	private int lineLength = 60;
	private boolean onlyShowCastableSpells;
	private boolean reloadGrantedSpells;
	private String strNoSpells;
	private String strPrefix;

	public ListSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		onlyShowCastableSpells = getConfigBoolean("only-show-castable-spells", false);
		reloadGrantedSpells = config.getBoolean("spells." + spellName + ".reload-granted-spells", false);
		strNoSpells = config.getString("spells." + spellName + ".str-no-spells", "You do not know any spells.");
		strPrefix = config.getString("spells." + spellName + ".str-prefix", "Known spells:");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			String extra = "";
			if (args != null && args.length > 0 && spellbook.hasAdvancedPerm()) {
				Player p = Bukkit.getServer().getPlayer(args[0]);
				if (p != null) {
					spellbook = MagicSpells.getSpellbook(p);
					extra = "(" + p.getDisplayName() + ") ";
				}
			}
			if (spellbook != null && reloadGrantedSpells) {
				spellbook.addGrantedSpells();
			}
			if (spellbook == null || spellbook.getSpells().size() == 0) {
				// no spells
				sendMessage(player, strNoSpells);
			} else {
				String s = "";
				for (Spell spell : spellbook.getSpells()) {
					if (!onlyShowCastableSpells || spellbook.canCast(spell)) {
						if (s.equals("")) {
							s = spell.getName();
						} else {
							s += ", " + spell.getName();
						}
					}
				}
				s = strPrefix + " " + extra + s;
				while (s.length() > lineLength) {
					int i = s.substring(0, lineLength).lastIndexOf(' ');
					sendMessage(player, s.substring(0, i));
					s = s.substring(i+1);
				}
				if (s.length() > 0) {
					sendMessage(player, s);
				}
			}
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		StringBuilder s = new StringBuilder();
		
		// get spell list
		Collection<Spell> spells = MagicSpells.spells();
		if (args != null && args.length > 0) {
			Player p = Bukkit.getServer().getPlayer(args[0]);
			if (p == null) {
				sender.sendMessage("No such player.");
				return true;
			} else {
				spells = MagicSpells.getSpellbook(p).getSpells();
				s.append(p.getName() + "'s spells: ");
			}
		} else {
			s.append("All spells: ");
		}
		
		// create string of spells
		for (Spell spell : spells) {
			s.append(spell.getName());
			s.append(" ");
		}
		
		// send message
		sender.sendMessage(s.toString());
		
		return true;
	}

}
