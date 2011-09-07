package com.nisovin.oldgods;

import org.bukkit.event.Event;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.plugin.PluginManager;

import com.nisovin.oldgods.godhandlers.*;

public class EListener extends EntityListener {

	OldGods plugin;
	
	public EListener(OldGods plugin) {
		this.plugin = plugin;
		PluginManager pm = plugin.getServer().getPluginManager();
		pm.registerEvent(Event.Type.CREATURE_SPAWN, this, Event.Priority.Monitor, plugin);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.ENTITY_TARGET, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.ENTITY_COMBUST, this, Event.Priority.Normal, plugin);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this, Event.Priority.Normal, plugin);
	}

	@Override
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.DEATH) {
			DeathHandler.onCreatureSpawn(event);
		}
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.isCancelled()) return;
		
		if (event instanceof EntityDamageByEntityEvent) {
			onEntityDamageByEntity((EntityDamageByEntityEvent)event);
		}
		
		God god = plugin.currentGod();
		
		if (god == God.HEALING) {
			HealingHandler.onEntityDamage(event);
		} else if (god == God.EXPLORATION) {
			ExplorationHandler.onEntityDamage(event);
		} else if (god == God.MINING) {
			MiningHandler.onEntityDamage(event);
		} else if (god == God.OCEAN) {
			OceanHandler.onEntityDamage(event);
		}
	}
	
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		God god = plugin.currentGod();		
		
		if (god == God.DEATH) {
			DeathHandler.onEntityDamageByEntity(event);
		} else if (god == God.HUNT) {
			HuntHandler.onEntityDamageByEntity(event);
		} else if (god == God.WAR) {
			WarHandler.onEntityDamageByEntity(event);
		}
		
	}

	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.LOVE) {
			LoveHandler.onEntityTarget(event);
		}
	}

	@Override
	public void onEntityCombust(EntityCombustEvent event) {
		if (event.isCancelled()) return;
		
		God god = plugin.currentGod();
		
		if (god == God.DEATH) {
			DeathHandler.onEntityCombust(event);
		}
	}

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		God god = plugin.currentGod();
		
		if (god == God.HUNT) {
			HuntHandler.onEntityDeath(event);
		} else if (god == God.COOKING) {
			CookingHandler.onEntityDeath(event);
		}
	}
	
	
	
}
