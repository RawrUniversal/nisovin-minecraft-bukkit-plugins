package com.nisovin.MagicSpells.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.nisovin.MagicSpells.Spell;

@SuppressWarnings("serial")
public class SpellCastEvent extends SpellEvent implements Cancellable {

	private boolean cancelled = false;
	private int cooldown;
	private boolean chargeReagents;
	
	public SpellCastEvent(Spell spell, Player caster, Spell.SpellCastState state, int cooldown, boolean chargeReagents) {
		super("SpellCast", spell, caster);
		this.cooldown = cooldown;
		this.chargeReagents = chargeReagents;
	}
	
	public int getCooldown() {
		return cooldown;
	}
	
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	public boolean chargeReagents() {
		return chargeReagents;
	}
	
	public void setChargeReagents(boolean chargeReagents) {
		this.chargeReagents = chargeReagents;
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
