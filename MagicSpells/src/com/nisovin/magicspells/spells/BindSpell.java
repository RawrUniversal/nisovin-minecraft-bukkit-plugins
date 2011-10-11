package com.nisovin.magicspells.spells;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.CommandSpell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.util.MagicConfig;

public class BindSpell extends CommandSpell {
	
	private String strUsage;
	private String strNoSpell;
	private String strCantBind;

	public BindSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		strUsage = config.getString("spells." + spellName + ".str-usage", "You must specify a spell name and hold an item in your hand.");
		strNoSpell = config.getString("spells." + spellName + ".str-no-spell", "You do not know a spell by that name.");
		strCantBind = config.getString("spells." + spellName + ".str-cant-bind", "That spell cannot be bound to an item.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length != 1) {
				sendMessage(player, strUsage);
			} else {
				Spell spell = MagicSpells.getSpellByInGameName(args[0]);
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
					int typeId = player.getItemInHand()==null?-1:player.getItemInHand().getTypeId();
					spellbook.removeSpell(spell);
					if (typeId <= 0) {
						spellbook.addSpell(spell, -1);
					} else {
						spellbook.addSpell(spell, player.getItemInHand().getTypeId());
					}
					spellbook.save();
					sendMessage(player, formatMessage(strCastSelf, "%s", spell.getName()));
					return PostCastAction.NO_MESSAGES;
				}
			}
			return PostCastAction.ALREADY_HANDLED;
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}

}
