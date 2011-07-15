package com.nisovin.MagicSpells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class PainSpell extends InstantSpell {

	private int damage;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private String strNoTarget;
	
	public PainSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		damage = getConfigInt(config, "damage", 4);
		obeyLos = getConfigBoolean(config, "obey-los", true);
		targetPlayers = getConfigBoolean(config, "target-players", false);
		checkPlugins = getConfigBoolean(config, "check-plugins", true);
		strNoTarget = getConfigString(config, "str-no-target", "No target found.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target == null) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				return true;
			} else {
				if (target instanceof Player && checkPlugins) {
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.CUSTOM, damage);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return true;
					}
				}
				target.damage(damage, player);
			}
		}
		return false;
	}

}
