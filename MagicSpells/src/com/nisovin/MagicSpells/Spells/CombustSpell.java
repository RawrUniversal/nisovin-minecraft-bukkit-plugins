package com.nisovin.MagicSpells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class CombustSpell extends InstantSpell {
	
	private boolean targetPlayers;
	private int fireTicks;
	private boolean obeyLos;
	private boolean checkPlugins;
	private String strNoTarget;
	
	public CombustSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		fireTicks = config.getInt("spells." + spellName + ".fire-ticks", 100);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		checkPlugins = config.getBoolean("spells." + spellName + ".check-plugins", true);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "");
	}
	
	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range>0?range:100, targetPlayers, obeyLos);
			if (target == null) {
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				if (target instanceof Player && checkPlugins) {
					// call other plugins
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, 1);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
				target.setFireTicks(fireTicks);
				// TODO: manually send messages with replacements
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
}