package com.nisovin.magicspells.spells.instant;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class RoarSpell extends InstantSpell {

	private String strNoTarget;
	
	public RoarSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		strNoTarget = getConfigString("str-no-target", "No targets found.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int count = 0;
			List<Entity> entities = player.getNearbyEntities(range, range, range);
			for (Entity entity : entities) {
				if (entity instanceof Monster) {
					((Monster) entity).setTarget(player);
					count++;
				}
			}
			
			if (count == 0) {
				// nothing affected
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
