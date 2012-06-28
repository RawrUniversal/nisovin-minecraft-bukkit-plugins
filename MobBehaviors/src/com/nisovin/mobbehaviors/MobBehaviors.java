package com.nisovin.mobbehaviors;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.PathfinderGoalSelector;

import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.mobbehaviors.behaviors.*;

public class MobBehaviors extends JavaPlugin implements Listener {
	
	protected HashMap<String, Class<? extends Behavior>> registeredBehaviors = new HashMap<String, Class<? extends Behavior>>();
	protected HashMap<String, BehaviorSet> behaviorSets = new HashMap<String, BehaviorSet>();
	protected HashMap<String, WorldBehaviors> worldBehaviors = new HashMap<String, WorldBehaviors>();
	
	@Override
	public void onEnable() {
		
		registerBehavior("Swim", BehaviorSwim.class);
		registerBehavior("MeleeAttack", BehaviorMeleeAttack.class);
		registerBehavior("Wander", BehaviorWander.class);
		registerBehavior("LookAtPlayer", BehaviorLookAt.class);
		registerBehavior("LookAround", BehaviorLookAround.class);
		
		getServer().getPluginManager().registerEvents(this, this);
		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			public void run() {
				loadConfig();
			}
		}, 1);
		
	}
	
	public void loadConfig() {
		behaviorSets.clear();
		worldBehaviors.clear();
		
		saveDefaultConfig();
		
		Configuration config = getConfig();
		
		// get behavior sets
		ConfigurationSection sec = config.getConfigurationSection("BehaviorSets");
		Set<String> keys = sec.getKeys(false);
		for (String key : keys) {
			ConfigurationSection behaviorSec = sec.getConfigurationSection(key);
			BehaviorSet set = new BehaviorSet(behaviorSec, this);
			behaviorSets.put(key, set);
		}
		
		// get world behaviors
		sec = config.getConfigurationSection("Worlds");
		keys = sec.getKeys(false);
		for (String worldName : keys) {
			ConfigurationSection worldSec = sec.getConfigurationSection(worldName);
			WorldBehaviors behaviors = new WorldBehaviors(worldSec, this);
			worldBehaviors.put(worldName, behaviors);
		}
	}
	
	public void registerBehavior(String name, Class<? extends Behavior> behavior) {
		registeredBehaviors.put(name, behavior);
	}
	
	private List<Behavior> getApplicableBehaviors(EntityType creatureType, Location location) {
		WorldBehaviors behaviors = worldBehaviors.get(location.getWorld().getName());
		if (behaviors == null) {
			// no behaviors defined for this world
			return null;
		} else {
			// get all sets that match this location
			List<BehaviorSet> sets = behaviors.getApplicableBehaviorSets(location);
			if (sets == null || sets.size() == 0) {
				// there are no behavior sets for this location
				return null;
			} else {
				// try to find a set that has behaviors for this creature
				for (BehaviorSet set : sets) {
					List<Behavior> b = set.getBehaviors(creatureType);
					if (b != null) {
						return b;
					}
				}
				// no behaviors for this creature
				return null;
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	@EventHandler(priority=EventPriority.MONITOR)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if (event.isCancelled()) return;
		
		// get creature goals
		EntityType creatureType = event.getEntityType();
		List<Behavior> behaviors = getApplicableBehaviors(creatureType, event.getLocation());
		if (behaviors == null) return;

		System.out.println("got behaviors");
		
		// get entity
		LivingEntity livingEntity = (LivingEntity)event.getEntity();
		EntityLiving entityLiving = ((CraftLivingEntity)livingEntity).getHandle();
		
		// get entity's goal selector
		PathfinderGoalSelector selector = null;
		try {
			Field field = EntityLiving.class.getDeclaredField("goalSelector");
			field.setAccessible(true);
			selector = (PathfinderGoalSelector)field.get(entityLiving);
		} catch (Exception e) {
			getLogger().warning("Entity has no goalSelector");
			e.printStackTrace();
			return;
		}		
		if (selector == null) return;
		
		System.out.println("got goal selector");
		
		// clear the selector
		try {
			Field field1 = PathfinderGoalSelector.class.getDeclaredField("a");
			field1.setAccessible(true);
			((ArrayList)field1.get(selector)).clear();
			Field field2 = PathfinderGoalSelector.class.getDeclaredField("b");
			field2.setAccessible(true);
			((ArrayList)field2.get(selector)).clear();
		} catch (Exception e) {
			getLogger().warning("Could not access and clear entity's goal selector data");
			e.printStackTrace();
			return;
		}
		
		System.out.println("cleared selector");
		
		// add goals
		for (Behavior behavior : behaviors) {
			try {
				behavior.addGoalToEntity(entityLiving, selector);
				System.out.println("added behavior " + behavior.getClass().getName());
			} catch (Exception e) {
				getLogger().warning("Failed to add behavior '" + behavior.getClass().getName() + "' to entity (" + creatureType.getName() + ")");
				e.printStackTrace();
			}
		}		
	}

	@Override
	public void onDisable() {
	}

}
