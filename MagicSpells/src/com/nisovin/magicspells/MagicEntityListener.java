package com.nisovin.magicspells;

import java.util.HashSet;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import com.nisovin.magicspells.util.MagicListener;

public class MagicEntityListener extends EntityListener implements MagicListener {
	
	private boolean disabled = false;
	
	public MagicEntityListener(MagicSpells plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_TARGET, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_COMBUST, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.EXPLOSION_PRIME, this, Event.Priority.Normal, plugin);
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.ENTITY_DAMAGE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityDamage(event);
			}
		}
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.ENTITY_TARGET);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityTarget(event);
			}
		}		
	}
	
	@Override
	public void onEntityCombust(EntityCombustEvent event) {
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.ENTITY_COMBUST);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityCombust(event);
			}
		}		
	}
	
	@Override
	public void onExplosionPrime(ExplosionPrimeEvent event) {
		if (disabled) return;
		
		HashSet<Spell> spells = MagicSpells.listeners.get(Event.Type.EXPLOSION_PRIME);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onExplosionPrime(event);
			}
		}		
	}

	@Override
	public void disable() {
		disabled = true;
	}
	
}