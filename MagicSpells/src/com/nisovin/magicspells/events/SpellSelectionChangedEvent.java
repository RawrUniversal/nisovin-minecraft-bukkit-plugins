package com.nisovin.magicspells.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.CastItem;

public class SpellSelectionChangedEvent extends SpellEvent {

    private static final HandlerList handlers = new HandlerList();
    
    private CastItem castItem;
    
	public SpellSelectionChangedEvent(Spell spell, Player caster, CastItem castItem) {
		super(spell, caster);
		this.castItem = castItem;
	}
	
	public CastItem getCastItem() {
		return castItem;
	}
	
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
