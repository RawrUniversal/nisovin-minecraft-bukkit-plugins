package com.nisovin.magicspells.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.SpellReagents;

@SuppressWarnings("serial")
public class SpellCastEvent extends SpellEvent implements Cancellable {

	private float power;
	private int cooldown;
	private SpellReagents reagents;
	private boolean cancelled = false;
	
	public SpellCastEvent(Spell spell, Player caster, Spell.SpellCastState state, float power, int cooldown, SpellReagents reagents) {
		super("SpellCast", spell, caster);
		this.cooldown = cooldown;
		this.reagents = reagents;
		this.power = power;
	}
	
	/**
	 * Gets the current power level of the spell. Spells start at a power level of 1.0.
	 * @return the power level
	 */
	public float getPower() {
		return power;
	}
	
	/**
	 * Sets the power level for the spell being cast.
	 * @param power the power level
	 */
	public void setPower(float power) {
		this.power = power;
	}
	
	/**
	 * Increases the power lever for the spell being cast by the given multiplier.
	 * @param power the power level multiplier
	 */
	public void increasePower(float power) {
		this.power *= power;
	}
	
	/**
	 * Gets the cooldown that will be triggered after the spell is cast.
	 * @return the cooldown
	 */
	public int getCooldown() {
		return cooldown;
	}
	
	/**
	 * Sets the cooldown that will be triggered after the spell is cast.
	 * @param cooldown the cooldown to set
	 */
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	/**
	 * Gets the reagents that will be charged after the spell is cast. This can be modified.
	 * @return the reagents
	 */
	public SpellReagents getReagents() {
		return reagents;
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
