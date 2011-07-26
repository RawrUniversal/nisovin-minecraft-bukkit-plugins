package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class ConfusionSpell extends InstantSpell {

	public ConfusionSpell(Configuration config, String spellName) {
		super(config, spellName);
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			List<Entity> entities = player.getNearbyEntities(range, range, range);
			List<Monster> monsters = new ArrayList<Monster>();
			for (Entity e : entities) {
				if (e instanceof Monster) {
					monsters.add((Monster)e);
				}
			}
			for (int i = 0; i < monsters.size(); i++) {
				int next = i+1;
				if (next >= monsters.size()) {
					next = 0;
				}
				monsters.get(i).setTarget(monsters.get(next));
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
