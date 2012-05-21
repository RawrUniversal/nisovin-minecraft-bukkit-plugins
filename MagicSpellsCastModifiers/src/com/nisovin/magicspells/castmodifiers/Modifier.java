package com.nisovin.magicspells.castmodifiers;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellCastEvent;

public class Modifier {

	Condition condition;
	String conditionVar;
	ModifierType type;
	String modifierVar;
	
	public Modifier(String s) {
		String[] data = s.split(" ");		
		condition = Condition.getConditionByName(data[0]);		
		type = getTypeByName(data[1]);
		if (type == null && data.length > 2) {
			conditionVar = data[1];
			type = getTypeByName(data[2]);
			if (data.length > 3) {
				modifierVar = data[3];
			}
		} else if (data.length > 2) {
			modifierVar = data[2];
		}
	}
	
	public boolean apply(SpellCastEvent event) {
		Player player = event.getCaster();
		boolean check = condition.check(player, conditionVar);
		if (check == false && type == ModifierType.REQUIRED) {
			event.setCancelled(true);
			return false;
		} else if (check == true && type == ModifierType.DENIED) {
			event.setCancelled(true);
			return false;
		} else if (type == ModifierType.POWER) {
			event.increasePower(Float.parseFloat(modifierVar));
		} else if (type == ModifierType.COOLDOWN) {
			event.setCooldown(Integer.parseInt(modifierVar));
		}
		return true;
	}
	
	public boolean apply(ManaChangeEvent event) {
		Player player = event.getPlayer();
		boolean check = condition.check(player, conditionVar);
		if (check == false && type == ModifierType.REQUIRED) {
			event.setNewAmount(event.getOldAmount());
			return false;
		} else if (check == true && type == ModifierType.DENIED) {
			event.setNewAmount(event.getOldAmount());
			return false;
		} else if (type == ModifierType.POWER) {
			int gain = event.getNewAmount() - event.getOldAmount();
			gain = (int)(gain * Float.parseFloat(modifierVar));
			int newAmt = event.getOldAmount() + gain;
			if (newAmt > event.getMaxMana()) newAmt = event.getMaxMana();
			event.setNewAmount(newAmt);
		}
		return true;
	}
	
	private ModifierType getTypeByName(String name) {
		if (name.equalsIgnoreCase("required") || name.equalsIgnoreCase("require")) {
			return ModifierType.REQUIRED;
		} else if (name.equalsIgnoreCase("denied") || name.equalsIgnoreCase("deny")) {
			return ModifierType.DENIED;
		} else if (name.equalsIgnoreCase("power") || name.equalsIgnoreCase("empower") || name.equalsIgnoreCase("multiply")) {
			return ModifierType.POWER;
		} else if (name.equalsIgnoreCase("cooldown")) {
			return ModifierType.COOLDOWN;
		} else {
			return null;
		}
	}
	
	private enum ModifierType {
		REQUIRED,
		DENIED,
		POWER,
		COOLDOWN
	}
	
}
