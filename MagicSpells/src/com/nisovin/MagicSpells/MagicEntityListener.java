package com.nisovin.MagicSpells;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;

public class MagicEntityListener extends EntityListener {
	
	public MagicEntityListener(MagicSpells plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_TARGET, this, Event.Priority.Normal, plugin);
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.ENTITY_DAMAGE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityDamage(event);
			}
		}
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.ENTITY_TARGET);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityTarget(event);
			}
		}		
	}
	
}