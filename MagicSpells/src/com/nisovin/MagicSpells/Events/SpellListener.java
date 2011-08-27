package com.nisovin.MagicSpells.Events;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

/**
 * Handles events related to spells
 * 
 */
public class SpellListener extends CustomEventListener {

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof SpellTargetEvent) {
			onSpellTarget((SpellTargetEvent)event);
		} else if (event instanceof SpellCastEvent) {
			onSpellCast((SpellCastEvent)event);
		}
	}
	
	/**
	 * Called when an instant spell targets a LivingEntity.
	 * @param event relevant event details
	 */
	public void onSpellTarget(SpellTargetEvent event) {
		
	}
	
	/**
	 * Called when a spell is cast.
	 * @param event relevant event details
	 */
	public void onSpellCast(SpellCastEvent event) {
		
	}
	
}
