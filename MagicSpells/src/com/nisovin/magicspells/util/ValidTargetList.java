package com.nisovin.magicspells.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;

public class ValidTargetList {

	static Map<String, EntityType> typeMap = new HashMap<String, EntityType>();
	static {
		for (EntityType type : EntityType.values()) {
			typeMap.put(type.getName().toLowerCase(), type);
		}
		typeMap.put("zombiepig", EntityType.PIG_ZOMBIE);
		typeMap.put("mooshroom", EntityType.MUSHROOM_COW);
		typeMap.put("cat", EntityType.OCELOT);
		typeMap.put("ocelot", EntityType.OCELOT);
		typeMap.put("golem", EntityType.IRON_GOLEM);
		typeMap.put("irongolem", EntityType.IRON_GOLEM);
		typeMap.put("snowgolem", EntityType.SNOWMAN);
		for (String s : typeMap.keySet()) {
			typeMap.put(s + "s", typeMap.get(s));
		}
		typeMap.put("endermen", EntityType.ENDERMAN);
	}
	
	boolean targetSelf = false;
	boolean targetPlayers = false;
	boolean targetNonPlayers = false;
	boolean targetMonsters = false;
	boolean targetAnimals = false;
	Set<EntityType> types = new HashSet<EntityType>();
	
	public ValidTargetList(Spell spell, String list) {
		if (list != null) {
			String[] ss = list.replace(" ", "").split(",");
			for (String s : ss) {
				if (s.equalsIgnoreCase("self") || s.equalsIgnoreCase("caster")) {
					targetSelf = true;
				} else if (s.equalsIgnoreCase("players") || s.equalsIgnoreCase("player")) {
					targetPlayers = true;
				} else if (s.equalsIgnoreCase("nonplayers") || s.equalsIgnoreCase("nonplayer")) {
					targetNonPlayers = true;
				} else if (s.equalsIgnoreCase("monsters") || s.equalsIgnoreCase("monster")) {
					targetMonsters = true;
				} else if (s.equalsIgnoreCase("animals") || s.equalsIgnoreCase("animal")) {
					targetAnimals = true;
				} else {
					EntityType type = typeMap.get(s.toLowerCase());
					if (type != null) {
						types.add(type);
					} else {
						MagicSpells.error("Invalid target type '" + s + "' on spell '" + spell.getInternalName() + "'");
					}
				}
			}
		}
	}
	
	public ValidTargetList(boolean targetPlayers, boolean targetNonPlayers) {
		this.targetPlayers = targetPlayers;
		this.targetNonPlayers = targetNonPlayers;
	}
	
	public boolean canTarget(Player caster, LivingEntity target) {
		return canTarget(caster, target, targetPlayers);
	}
	
	public boolean canTarget(Player caster, LivingEntity target, boolean targetPlayers) {
		if (targetSelf && target.equals(caster)) {
			return true;
		} else if (targetPlayers && target instanceof Player) {
			return true;
		} else if (targetNonPlayers && !(target instanceof Player)) {
			return true;
		} else if (targetMonsters && target instanceof Monster) {
			return true;
		} else if (targetAnimals && target instanceof Animals) {
			return true;
		} else if (types.contains(target.getType())) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canTarget(LivingEntity target) {
		if (targetPlayers && target instanceof Player) {
			return true;
		} else if (targetNonPlayers && !(target instanceof Player)) {
			return true;
		} else if (targetMonsters && target instanceof Monster) {
			return true;
		} else if (targetAnimals && target instanceof Animals) {
			return true;
		} else if (types.contains(target.getType())) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<LivingEntity> filterTargetList(Player caster, List<Entity> targets) {
		return filterTargetList(caster, targets, targetPlayers);
	}
	
	public List<LivingEntity> filterTargetList(Player caster, List<Entity> targets, boolean targetPlayers) {
		List<LivingEntity> realTargets = new ArrayList<LivingEntity>();
		for (Entity e : targets) {
			if (e instanceof LivingEntity && canTarget(caster, (LivingEntity)e, targetPlayers)) {
				realTargets.add((LivingEntity)e);
			}
		}
		return realTargets;
	}
	
	public boolean canTargetPlayers() {
		return targetPlayers;
	}
	
}
