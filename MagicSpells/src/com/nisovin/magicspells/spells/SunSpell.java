package com.nisovin.magicspells.spells;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.magicspells.ChanneledSpell;

public class SunSpell extends ChanneledSpell {

	private int timeToSet;
	private String strAnnounce;
		
	public SunSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		timeToSet = getConfigInt("time-to-set", 0);
		strAnnounce = getConfigString("str-announce", "The sun suddenly appears in the sky.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			String world = player.getWorld().getName();
			boolean success = addChanneler(world, player);
			if (!success) {
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
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
