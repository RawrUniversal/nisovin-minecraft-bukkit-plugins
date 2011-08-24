package com.nisovin.MagicSpells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class ForcetossSpell extends InstantSpell {

	private int damage;
	private float hForce;
	private float vForce;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private String strNoTarget;
	
	public ForcetossSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		damage = getConfigInt("damage", 0);
		hForce = getConfigInt("horizontal-force", 20) / 10.0F;
		vForce = getConfigInt("vertical-force", 10) / 10.0F;
		obeyLos = getConfigBoolean("obey-los", true);
		targetPlayers = getConfigBoolean("target-players", false);
		checkPlugins = getConfigBoolean("check-plugins", true);
		strNoTarget = getConfigString("str-no-target", "");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get target
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target == null) {
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// do damage
			if (damage > 0) {
				int damage = Math.round(this.damage*power);
				if (target instanceof Player && checkPlugins) {
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, target, DamageCause.ENTITY_ATTACK, damage);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						sendMessage(player, strNoTarget);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
				target.damage(damage);
			}
			
			// throw target
			Vector v = target.getLocation().toVector().subtract(player.getLocation().toVector())
					.setY(0)
					.normalize()
					.multiply(hForce*power)
					.setY(vForce*power);
			target.setVelocity(v);			
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	

}
