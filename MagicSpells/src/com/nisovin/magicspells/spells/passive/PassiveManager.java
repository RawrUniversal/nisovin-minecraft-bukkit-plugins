package com.nisovin.magicspells.spells.passive;

import java.util.Map;

import com.nisovin.magicspells.spells.PassiveSpell;

public class PassiveManager {

	Map<PassiveTrigger, PassiveListener> listeners;
	
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		PassiveListener listener = listeners.get(trigger);
		if (listener == null) {
			listener = trigger.getNewListener();
			listeners.put(trigger, listener);
		}
		listener.registerSpell(spell, var);
	}
	
}
