package com.nisovin.magicspells.variables;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;

public class GlobalVariable extends Variable {

	double value = 0;
	
	public GlobalVariable(double defaultValue, double minValue, double maxValue) {
		super(defaultValue, minValue, maxValue);
		value = defaultValue;
	}
	
	@Override
	public void modify(Player player, Spell spell, double amount) {
		value += amount;
		if (value > maxValue) {
			value = maxValue;
		} else if (value < minValue) {
			value = minValue;
		}
	}

	@Override
	public double getValue(Player player, Spell spell) {
		return value;
	}

	@Override
	public void reset(Player player, Spell spell) {
		value = defaultValue;
	}

}
