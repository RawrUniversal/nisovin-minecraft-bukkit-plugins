package com.nisovin.magicspells.memory;

import org.bukkit.event.Event;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellLearnEvent;
import com.nisovin.magicspells.events.SpellListener;

public class MemorySpellListener extends SpellListener {

	private MagicSpellsMemory plugin;
	
	public MemorySpellListener(MagicSpellsMemory plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, this, Event.Priority.Normal, plugin);
	}
	
	@Override
	public void onSpellLearn(SpellLearnEvent event) {
		int req = plugin.getRequiredMemory(event.getSpell());
		if (req > 0) {
			int mem = plugin.getMemoryRemaining(event.getLearner());
			MagicSpells.debug("Memory check: " + req + " required, " + mem + " remaining");
			if (mem < req) {
				event.setCancelled(true);
				MagicSpells.sendMessage(event.getLearner(), plugin.strOutOfMemory, "%spell", event.getSpell().getName());
			}
		}
	}

}
