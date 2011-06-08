package com.nisovin.MagicSpells.Spells;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class CombustSpell extends InstantSpell {

	private static final String SPELL_NAME = "combust";
	
	private boolean targetPlayers;
	private int fireTicks;
	private int precision;
	private boolean checkPlugins;
	private String strNoTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new CombustSpell(config, spellName));
		}
	}
	
	public CombustSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		fireTicks = config.getInt("spells." + spellName + ".fire-ticks", 100);
		precision = config.getInt("spells." + spellName + ".precision", 20);
		checkPlugins = config.getBoolean("spells." + spellName + ".check-plugins", true);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "");
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range>0?range:100, precision, targetPlayers);
			if (target == null) {
				sendMessage(player, strNoTarget);
				return true;
			} else {
				if (target instanceof Player && checkPlugins) {
					// call other plugins
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.FIRE_TICK, 1);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return true;
					}
				}
				target.setFireTicks(fireTicks);
				// TODO: manually send messages with replacements
			}
		}
		return false;
	}
}