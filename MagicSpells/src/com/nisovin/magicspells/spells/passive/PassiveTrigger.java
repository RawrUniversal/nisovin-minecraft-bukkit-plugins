package com.nisovin.magicspells.spells.passive;

import java.util.HashMap;
import java.util.Map;

public enum PassiveTrigger {
	
	TAKE_DAMAGE("takedamage", TakeDamageListener.class),
	GIVE_DAMAGE("givedamage", null),
	KILL("kill", null),
	RESPAWN("respawn", null),
	BLOCK_BREAK("blockbreak", null),
	BLOCK_PLACE("blockplace", null),
	RIGHT_CLICK("rightclick", null),
	RIGHT_CLICK_BLOCK_TYPE("rightclickblocktype", null),
	RIGHT_CLICK_BLOCK_COORD("rightclickblockcoord", null),
	RIGHT_CLICK_ENTITY("rightclickentity", null),
	SPELL_CAST("spellcast", null),
	SPELL_TARGET("spelltarget", null),
	SPRINT("sprint", null),
	STOP_SPRINT("stopsprint", null),
	SNEAK("sneak", null),
	STOP_SNEAK("stopsneak", null),
	HOT_BAR_SELECT("hotbarselect", null),
	HOT_BAR_DESELECT("hotbardeselect", null),
	BUFF("buff", null),
	TICKS("ticks", null);
	
	static Map<String, PassiveTrigger> map = new HashMap<String, PassiveTrigger>();
	
	public static PassiveTrigger getByName(String name) {
		return map.get(name);
	}
	
	String name;
	Class<? extends PassiveListener> listener;
	
	PassiveTrigger(String name, Class<? extends PassiveListener> listener) {
		this.name = name;
		this.listener = listener;
	}
	
	public String getName() {
		return name;
	}
	
	public PassiveListener getNewListener() {
		try {
			return listener.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
