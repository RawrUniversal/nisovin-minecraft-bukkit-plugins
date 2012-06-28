package com.nisovin.mobbehaviors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

public class BehaviorSet {

	private int maxHealth;
	private HashMap<EntityType, List<Behavior>> creatureTargetingBehaviors = new HashMap<EntityType, List<Behavior>>();
	private HashMap<EntityType, List<Behavior>> creaturePathingBehaviors = new HashMap<EntityType, List<Behavior>>();
	
	public BehaviorSet(ConfigurationSection config, MobBehaviors plugin) {
		Set<String> monsterConfigs = config.getKeys(false);
		for (String mc : monsterConfigs) {
			// get monster type
			EntityType creatureType = null;
			if (mc.equalsIgnoreCase("Zombie")) {
				creatureType = EntityType.ZOMBIE;
			} else {
				creatureType = EntityType.fromName(mc);
			}
			if (creatureType == null) {
				plugin.getLogger().warning("Unknown creature type '" + mc + "' in set '" + config.getName() + "'");
				continue;
			}
			
			// get data
			ConfigurationSection monsterSec = config.getConfigurationSection(mc);
			this.maxHealth = monsterSec.getInt("Health", 0);
			ConfigurationSection targetingSec = monsterSec.getConfigurationSection("Targeting");
			ConfigurationSection pathingSec = monsterSec.getConfigurationSection("Pathing");
			
			// add the targeting behaviors
			List<Behavior> targetingBehaviors = new ArrayList<Behavior>(); 
			Set<String> targetingKeys = targetingSec.getKeys(false);
			for (String key : targetingKeys) {
				ConfigurationSection behaviorSec = targetingSec.getConfigurationSection(key);
				String behaviorTypeStr = behaviorSec.getString("Type");
				Class<? extends Behavior> behaviorType = plugin.registeredBehaviors.get(behaviorTypeStr);
				if (behaviorType != null) {
					try {
						Behavior goal = behaviorType.getConstructor(ConfigurationSection.class).newInstance(behaviorSec);
						targetingBehaviors.add(goal);
					} catch (Exception e) {
						plugin.getLogger().severe("Failed to load behavior type '" + behaviorTypeStr + "' for '" + mc + "' in set '" + config.getName() + "'");
						e.printStackTrace();
						continue;
					}
				} else {
					plugin.getLogger().warning("Unknown behavior type '" + behaviorTypeStr + "' for '" + mc + "' in set '" + config.getName() + "'");
				}
			}
			creatureTargetingBehaviors.put(creatureType, targetingBehaviors);
			
			// add the pathing behaviors
			List<Behavior> pathingBehaviors = new ArrayList<Behavior>(); 
			Set<String> pathingKeys = pathingSec.getKeys(false);
			for (String key : pathingKeys) {
				ConfigurationSection behaviorSec = pathingSec.getConfigurationSection(key);
				String behaviorTypeStr = behaviorSec.getString("Type");
				Class<? extends Behavior> behaviorType = plugin.registeredBehaviors.get(behaviorTypeStr);
				if (behaviorType != null) {
					try {
						Behavior goal = behaviorType.getConstructor(ConfigurationSection.class).newInstance(behaviorSec);
						pathingBehaviors.add(goal);
					} catch (Exception e) {
						plugin.getLogger().severe("Failed to load behavior type '" + behaviorTypeStr + "' for '" + mc + "' in set '" + config.getName() + "'");
						e.printStackTrace();
						continue;
					}
				} else {
					plugin.getLogger().warning("Unknown behavior type '" + behaviorTypeStr + "' for '" + mc + "' in set '" + config.getName() + "'");
				}
			}
			creaturePathingBehaviors.put(creatureType, pathingBehaviors);
		}
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public List<Behavior> getBehaviors(EntityType creatureType) {
		return creatureTargetingBehaviors.get(creatureType);
	}
	
}
