package com.nisovin.magicspells.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.nisovin.magicspells.Spell;

@SuppressWarnings("serial")
public class SpellTargetEvent extends SpellEvent implements Cancellable {

	private LivingEntity target;
	private boolean cancelled = false;
	
	public SpellTargetEvent(Spell spell, Player caster, LivingEntity target) {
		super("MAGIC_SPELLS_SPELL_TARGET", spell, caster);
		this.target = target;
	}
	
	/**
	 * Gets the living entity that is being targeted by the spell.
	 * @return the targeted living entity
	 */
	public LivingEntity getTarget() {
		return target;
	}
	
	/**
	 * Sets the spell's target to the provided living entity.
	 * @param target the new target
	 */
	public void setTarget(LivingEntity target) {
		this.target = target;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;		
	}
	
}
