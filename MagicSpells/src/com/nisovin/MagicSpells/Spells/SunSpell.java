package com.nisovin.MagicSpells.Spells;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.ChanneledSpell;

public class SunSpell extends ChanneledSpell {

	private int timeToSet;
	private String strAnnounce;
		
	public SunSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		timeToSet = getConfigInt(config, "time-to-set", 0);
		strAnnounce = getConfigString(config, "str-announce", "The sun suddenly appears in the sky.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			String world = player.getWorld().getName();
			addChanneler(world, player);
		}
		return false;
	}

	@Override
	protected void finishSpell(String key, Location location) {
		World world = location.getWorld();
		world.setTime(timeToSet);
		for (Player p : world.getPlayers()) {
			sendMessage(p, strAnnounce);
		}
	}

}
