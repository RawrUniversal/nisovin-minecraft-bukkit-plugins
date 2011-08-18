package com.nisovin.MagicSpells.Events;

import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class SpellListener extends CustomEventListener {

	@Override
	public void onCustomEvent(Event event) {
		if (event instanceof SpellTargetEvent) {
			onSpellTarget((SpellTargetEvent)event);
		} else if (event instanceof SpellCastEvent) {
			onSpellCast((SpellCastEvent)event);
		}
	}
	
	public void onSpellTarget(SpellTargetEvent event) {
		
	}
	
	public void onSpellCast(SpellCastEvent event) {
		
	}
	
}
