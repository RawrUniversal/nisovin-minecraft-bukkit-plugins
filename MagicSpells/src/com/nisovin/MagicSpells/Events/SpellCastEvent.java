package com.nisovin.MagicSpells.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.nisovin.MagicSpells.Spell;
import com.nisovin.MagicSpells.Util.SpellReagents;

@SuppressWarnings("serial")
public class SpellCastEvent extends SpellEvent implements Cancellable {

	private double power;
	private int cooldown;
	private SpellReagents reagents;
	private boolean cancelled = false;
	
	public SpellCastEvent(Spell spell, Player caster, Spell.SpellCastState state, double power, int cooldown, SpellReagents reagents) {
		super("SpellCast", spell, caster);
		this.cooldown = cooldown;
		this.reagents = reagents;
		this.power = power;
	}
	
	public double getPower() {
		return power;
	}
	
	public void setPower(float power) {
		this.power = power;
	}
	
	public int getCooldown() {
		return cooldown;
	}
	
	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
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
