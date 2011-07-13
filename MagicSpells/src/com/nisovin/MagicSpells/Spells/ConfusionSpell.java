package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class ConfusionSpell extends InstantSpell {

	private static final String SPELL_NAME = "confusion";
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new ConfusionSpell(config, spellName));
		}
	}

	public ConfusionSpell(Configuration config, String spellName) {
		super(config, spellName);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
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
		return false;
	}

}
