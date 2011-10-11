package com.nisovin.magicspells.spells;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class RecallSpell extends InstantSpell {
	
	private boolean allowCrossWorld;
	private int maxRange;
	private String strNoMark;
	private String strOtherWorld;
	private String strTooFar;

	public RecallSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		allowCrossWorld = config.getBoolean("spells." + spellName + ".allow-cross-world", true);
		maxRange = config.getInt("spells." + spellName + ".max-range", 0);
		strNoMark = config.getString("spells." + spellName + ".str-no-mark", "You have no mark to recall to.");
		strOtherWorld = config.getString("spells." + spellName + ".str-other-world", "Your mark is in another world.");
		strTooFar = config.getString("spells." + spellName + ".str-too-far", "You mark is too far away.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (MarkSpell.marks == null || !MarkSpell.marks.containsKey(player.getName())) {
				// no mark
				sendMessage(player, strNoMark);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				Location mark = MarkSpell.marks.get(player.getName()).getLocation();
				if (mark == null) {
					sendMessage(player, strNoMark);
					return PostCastAction.ALREADY_HANDLED;
				} else if (!allowCrossWorld && !mark.getWorld().getName().equals(player.getLocation().getWorld().getName())) {
					// can't cross worlds
					sendMessage(player, strOtherWorld);
					return PostCastAction.ALREADY_HANDLED;
				} else if (maxRange > 0 && mark.toVector().distanceSquared(player.getLocation().toVector()) > maxRange*maxRange) {
					// too far
					sendMessage(player, strTooFar);
					return PostCastAction.ALREADY_HANDLED;
				} else {
					// all good!
					player.teleport(mark);
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
