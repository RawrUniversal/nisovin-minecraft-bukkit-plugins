package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.util.Util;

public class Modifier {

	Condition condition;
	ModifierType type;
	String modifierVar;
	float modifierVarFloat;
	int modifierVarInt;
	
	public static Modifier factory(String s) {
		Modifier m = new Modifier();
		String[] data = Util.splitParams(s);
		if (data.length < 2) return null;
				
		// get condition
		m.condition = Condition.getConditionByName(data[0]);
		if (m.condition == null) return null;
		
		// get type and vars
		m.type = getTypeByName(data[1]);
		if (m.type == null && data.length > 2) {
			boolean varok = m.condition.setVar(data[1]);
			if (!varok) return null;
			m.type = getTypeByName(data[2]);
			if (data.length > 3) {
				m.modifierVar = data[3];
			}
		} else if (data.length > 2) {
			m.modifierVar = data[2];
		}
		
		// check type
		if (m.type == null) return null;
		
		// process modifiervar
		try {
			if (m.type == ModifierType.POWER || m.type == ModifierType.COOLDOWN || m.type == ModifierType.REAGENTS) {
				m.modifierVarFloat = Float.parseFloat(m.modifierVar);
			} else if (m.type == ModifierType.CAST_TIME) {
				m.modifierVarInt = Integer.parseInt(m.modifierVar);
			}
		} catch (NumberFormatException e) {
			return null;
		}
		
		// done
		return m;
	}
	
	public boolean apply(SpellCastEvent event) {
		Player player = event.getCaster();
		boolean check = condition.check(player);
		if (check == false && type == ModifierType.REQUIRED) {
			event.setCancelled(true);
			return false;
		} else if (check == true && type == ModifierType.DENIED) {
			event.setCancelled(true);
			return false;
		} else if (check == true) {
			if (type == ModifierType.STOP) {
				return false;
			} else if (type == ModifierType.POWER) {
				event.increasePower(modifierVarFloat);
			} else if (type == ModifierType.COOLDOWN) {
				event.setCooldown(modifierVarFloat);
			} else if (type == ModifierType.REAGENTS) {
				event.setReagents(event.getReagents().multiply(modifierVarFloat));
			} else if (type == ModifierType.CAST_TIME) {
				event.setCastTime(modifierVarInt);
			}
		} else if (check == false && type == ModifierType.CONTINUE) {
			return false;
		}
		return true;
	}
	
	public boolean apply(ManaChangeEvent event) {
		Player player = event.getPlayer();
		boolean check = condition.check(player);
		if (check == false && type == ModifierType.REQUIRED) {
			event.setNewAmount(event.getOldAmount());
			return false;
		} else if (check == true && type == ModifierType.DENIED) {
			event.setNewAmount(event.getOldAmount());
			return false;
		} else if (check == true && type == ModifierType.STOP) {
			return false;
		} else if (check == false && type == ModifierType.CONTINUE) {
			return false;
		} else if (check == true && type == ModifierType.POWER) {
			int gain = event.getNewAmount() - event.getOldAmount();
			gain = Math.round(gain * modifierVarFloat);
			int newAmt = event.getOldAmount() + gain;
			if (newAmt > event.getMaxMana()) newAmt = event.getMaxMana();
			event.setNewAmount(newAmt);
		}
		return true;
	}
	
	private static ModifierType getTypeByName(String name) {
		if (name.equalsIgnoreCase("required") || name.equalsIgnoreCase("require")) {
			return ModifierType.REQUIRED;
		} else if (name.equalsIgnoreCase("denied") || name.equalsIgnoreCase("deny")) {
			return ModifierType.DENIED;
		} else if (name.equalsIgnoreCase("power") || name.equalsIgnoreCase("empower") || name.equalsIgnoreCase("multiply")) {
			return ModifierType.POWER;
		} else if (name.equalsIgnoreCase("cooldown")) {
			return ModifierType.COOLDOWN;
		} else if (name.equalsIgnoreCase("reagents")) {
			return ModifierType.REAGENTS;
		} else if (name.equalsIgnoreCase("casttime")) {
			return ModifierType.CAST_TIME;
		} else if (name.equalsIgnoreCase("stop")) {
			return ModifierType.STOP;
		} else if (name.equalsIgnoreCase("continue")) {
			return ModifierType.CONTINUE;
		} else {
			return null;
		}
	}
	
	private enum ModifierType {
		REQUIRED,
		DENIED,
		POWER,
		COOLDOWN,
		REAGENTS,
		CAST_TIME,
		STOP,
		CONTINUE
	}
	
}
