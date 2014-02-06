package com.nisovin.magicspells.variables;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;

public class PlayerVariable extends Variable {

	Map<String, Double> map = new HashMap<String, Double>();

	public PlayerVariable(double defaultValue, double minValue, double maxValue) {
		super(defaultValue, minValue, maxValue);
	}
	
	@Override
	public void modify(Player player, Spell spell, double amount) {
		double value = getValue(player, spell);
		value += amount;
		if (value > maxValue) {
			value = maxValue;
		} else if (value < minValue) {
			value = minValue;
		}
	}

	@Override
	public double getValue(Player player, Spell spell) {
		if (map.containsKey(player.getName())) {
			return map.get(player.getName()).doubleValue();
		} else {
			return defaultValue;
		}
	}

	@Override
	public void reset(Player player, Spell spell) {
		map.remove(player.getName());
	}

}
