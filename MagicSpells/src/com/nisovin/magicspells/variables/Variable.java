package com.nisovin.magicspells.variables;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;

public abstract class Variable {

	protected double defaultValue = 0;
	protected double maxValue = Double.MAX_VALUE;
	protected double minValue = 0;
	
	public Variable(double defaultValue, double minValue, double maxValue) {
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	public abstract void modify(Player player, Spell spell, double amount);
	
	public abstract double getValue(Player player, Spell spell);
	
	public abstract void reset(Player player, Spell spell);
	
}
