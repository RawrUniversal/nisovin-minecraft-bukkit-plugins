package com.nisovin.magicspells.spells.instant;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class PainSpell extends InstantSpell {

	private int damage;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private String strNoTarget;
	
	public PainSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		damage = getConfigInt("damage", 4);
		obeyLos = getConfigBoolean("obey-los", true);
		targetPlayers = getConfigBoolean("target-players", false);
		checkPlugins = getConfigBoolean("check-plugins", true);
		strNoTarget = getConfigString("str-no-target", "No target found.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target == null) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				int dam = Math.round(damage*power);
				if (target instanceof Player && checkPlugins) {
					// handle the event myself so I can detect cancellation properly
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, dam);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return PostCastAction.ALREADY_HANDLED;
					}
					dam = event.getDamage();
				}
				target.damage(dam);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
