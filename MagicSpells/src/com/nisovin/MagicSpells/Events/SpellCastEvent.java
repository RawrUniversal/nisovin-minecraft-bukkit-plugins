package com.nisovin.MagicSpells.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.nisovin.MagicSpells.Spell;

@SuppressWarnings("serial")
public class SpellCastEvent extends SpellEvent implements Cancellable {

	private boolean cancelled = false;
	
	public SpellCastEvent(Spell spell, Player caster, Spell.SpellCastState state) {
		super("SpellCast", spell, caster);
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
