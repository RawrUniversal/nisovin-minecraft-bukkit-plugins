package com.nisovin.magicspells.castmodifiers;

import java.util.HashMap;

import org.bukkit.entity.Player;

public abstract class Condition {

	public abstract boolean check(Player player, String var);
	
	private static HashMap<String,Condition> conditions = new HashMap<String,Condition>();
	
	public static void addCondition(String name, Condition condition) {
		conditions.put(name.toLowerCase(), condition);
	}
	
	static Condition getConditionByName(String name) {
		return conditions.get(name.toLowerCase());
	}
	
	static {
		conditions.put("day", new DayCondition());
		conditions.put("night", new NightCondition());
		conditions.put("moonphase", new MoonPhaseCondition());
		conditions.put("lightlevelabove", new LightLevelAboveCondition());
		conditions.put("lightlevelbelow", new LightLevelBelowCondition());
		conditions.put("onblock", new OnBlockCondition());
		conditions.put("inblock", new InBlockCondition());
		conditions.put("permission", new PermissionCondition());
	}
	
}
