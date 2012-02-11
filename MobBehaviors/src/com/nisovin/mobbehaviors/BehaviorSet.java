package com.nisovin.mobbehaviors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.CreatureType;

public class BehaviorSet {

	private HashMap<CreatureType, List<Behavior>> creatureBehaviors = new HashMap<CreatureType, List<Behavior>>();
	
	public BehaviorSet(ConfigurationSection config, MobBehaviors plugin) {
		Set<String> monsterConfigs = config.getKeys(false);
		for (String mc : monsterConfigs) {
			// get monster type
			CreatureType creatureType = null;
			if (mc.equalsIgnoreCase("Zombie")) {
				creatureType = CreatureType.ZOMBIE;
			} else {
				creatureType = CreatureType.fromName(mc);
			}
			if (creatureType == null) {
				plugin.getLogger().warning("Unknown creature type '" + mc + "' in set '" + config.getName() + "'");
				continue;
			}
			
			// get goals
			List<Behavior> behaviors = new ArrayList<Behavior>(); 
			ConfigurationSection monsterSec = config.getConfigurationSection(mc);
			Set<String> behaviorKeys = monsterSec.getKeys(false);
			
			// add the goals
			for (String key : behaviorKeys) {
				ConfigurationSection behaviorSec = monsterSec.getConfigurationSection(key);
				String behaviorTypeStr = behaviorSec.getString("Type");
				Class<? extends Behavior> behaviorType = plugin.registeredBehaviors.get(behaviorTypeStr);
				if (behaviorType != null) {
					try {
						Behavior goal = behaviorType.getConstructor(ConfigurationSection.class).newInstance(behaviorSec);
						behaviors.add(goal);
					} catch (Exception e) {
						plugin.getLogger().severe("Failed to load behavior type '" + behaviorTypeStr + "' for '" + mc + "' in set '" + config.getName() + "'");
						e.printStackTrace();
						continue;
					}
				} else {
					plugin.getLogger().warning("Unknown behavior type '" + behaviorTypeStr + "' for '" + mc + "' in set '" + config.getName() + "'");
				}
			}
			
			// save goal set
			creatureBehaviors.put(creatureType, behaviors);
		}
	}
	
	public List<Behavior> getBehaviors(CreatureType creatureType) {
		return creatureBehaviors.get(creatureType);
	}
	
}
