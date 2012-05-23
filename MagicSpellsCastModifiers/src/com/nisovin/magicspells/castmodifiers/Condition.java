package com.nisovin.magicspells.castmodifiers;

import java.util.HashMap;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.castmodifiers.conditions.*;


public abstract class Condition {

	public abstract boolean check(Player player);
	
	public abstract void setVar(String var);
	
	private static HashMap<String, Class<? extends Condition>> conditions = new HashMap<String, Class<? extends Condition>>();
	
	public static void addCondition(String name, Class<? extends Condition> condition) {
		conditions.put(name.toLowerCase(), condition);
	}
	
	static Condition getConditionByName(String name) {
		Class<? extends Condition> clazz = conditions.get(name.toLowerCase());
		if (clazz == null) {
			return null;
		}
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	static {
		conditions.put("day", DayCondition.class);
		conditions.put("night", NightCondition.class);
		conditions.put("storm", StormCondition.class);
		conditions.put("moonphase", MoonPhaseCondition.class);
		conditions.put("lightlevelabove", LightLevelAboveCondition.class);
		conditions.put("lightlevelbelow", LightLevelBelowCondition.class);
		conditions.put("onblock", OnBlockCondition.class);
		conditions.put("inblock", InBlockCondition.class);
		conditions.put("healthabove", HealthAboveCondition.class);
		conditions.put("healthbelow", HealthBelowCondition.class);
		conditions.put("permission", PermissionCondition.class);
		conditions.put("wearing", WearingCondition.class);
		conditions.put("world", InWorldCondition.class);
	}
	
}
