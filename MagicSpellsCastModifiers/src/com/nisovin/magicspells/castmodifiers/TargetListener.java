package com.nisovin.magicspells.castmodifiers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.events.SpellTargetEvent;

public class TargetListener implements Listener {

	private MagicSpellsCastModifiers plugin;
	
	public TargetListener(MagicSpellsCastModifiers plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onSpellTarget(SpellTargetEvent event) {
		ModifierSet m = plugin.targetModifiers.get(event.getSpell());
		if (m != null) {
			m.apply(event);
		}
	}
}
