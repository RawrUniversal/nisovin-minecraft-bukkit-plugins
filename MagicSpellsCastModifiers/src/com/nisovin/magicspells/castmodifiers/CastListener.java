package com.nisovin.magicspells.castmodifiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.events.SpellCastEvent;

public class CastListener implements Listener {
	
	private MagicSpellsCastModifiers plugin;
	
	public CastListener(MagicSpellsCastModifiers plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onSpellCast(SpellCastEvent event) {
		ModifierSet m = plugin.modifiers.get(event.getSpell());
		if (m != null) {
			m.apply(event);
		}
	}
}
