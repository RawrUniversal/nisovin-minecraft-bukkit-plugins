package com.nisovin.MagicSpells.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import com.nisovin.MagicSpells.Spell;

@SuppressWarnings("serial")
public class SpellEvent extends Event {

	private Spell spell;
	private Player caster;
	
	public SpellEvent(String type, Spell spell, Player caster) {
		super(type);
		this.spell = spell;
		this.caster = caster;
	}
	
	public Spell getSpell() {
		return spell;
	}
	
	public Player getCaster() {
		return caster;
	}
	
}
