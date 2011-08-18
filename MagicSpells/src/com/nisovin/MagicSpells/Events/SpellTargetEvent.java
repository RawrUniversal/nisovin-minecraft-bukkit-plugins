package com.nisovin.MagicSpells.Events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.nisovin.MagicSpells.Spell;

@SuppressWarnings("serial")
public class SpellTargetEvent extends SpellEvent implements Cancellable {

	private LivingEntity target;
	private boolean cancelled = false;
	
	public SpellTargetEvent(Spell spell, Player caster, LivingEntity target) {
		super("SpellTarget", spell, caster);
		this.target = target;
	}
	
	public LivingEntity getTarget() {
		return target;
	}
	
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
