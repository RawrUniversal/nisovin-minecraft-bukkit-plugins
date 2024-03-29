package com.nisovin.magicspells.spells.instant;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ForcepushSpell extends InstantSpell {
	
	private int radius;
	private int force;
	private int yForce;
	private int maxYForce;
	private boolean callTargetEvents;
	
	public ForcepushSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		radius = getConfigInt("range", 3);
		force = getConfigInt("pushback-force", 30);
		yForce = getConfigInt("additional-vertical-force", 15);
		maxYForce = getConfigInt("max-vertical-force", 20);
		callTargetEvents = getConfigBoolean("call-target-events", true);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			knockback(player, radius, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public void knockback(Player player, int range, float power) {
	    Vector p = player.getLocation().toVector();
		List<Entity> entities = player.getNearbyEntities(range, range, range);
		Vector e, v;
		for (Entity entity : entities) {
			if (entity instanceof LivingEntity && validTargetList.canTarget(player, (LivingEntity)entity)) {
				if (callTargetEvents) {
					SpellTargetEvent event = new SpellTargetEvent(this, player, (LivingEntity)entity);
					Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						continue;
					}
				}
				e = entity.getLocation().toVector();
				v = e.subtract(p).normalize().multiply(force/10.0*power);
				if (force != 0) {
					v.setY(v.getY() + (yForce/10.0*power));
				} else {
					v.setY(yForce/10.0*power);
				}
				if (v.getY() > (maxYForce/10.0)) {
					v.setY(maxYForce/10.0);
				}
				entity.setVelocity(v);
				playSpellEffects(EffectPosition.TARGET, entity);
			}
	    }
		playSpellEffects(EffectPosition.CASTER, player);
	}

}
