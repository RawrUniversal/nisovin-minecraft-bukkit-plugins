package com.nisovin.magicspells.variables;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;

public class VariableManager {

	Map<String, Variable> variables;
	
	public VariableManager(ConfigurationSection section) {
		for (String var : section.getKeys(false)) {
			String type = section.getString(var + ".type", "global");
			double def = section.getDouble(var + ".default", 0);
			double min = section.getDouble(var + ".min", 0);
			double max = section.getDouble(var + ".max", Double.MAX_VALUE);
			Variable variable;
			if (type.equalsIgnoreCase("player")) {
				variable = new PlayerVariable(def, min, max);
			} else {
				variable = new GlobalVariable(def, min, max);
			}
			variables.put(var, variable);
		}
	}
	
	public void modify(String variable, Player player, Spell spell, double amount) {
		Variable var = variables.get(variable);
		if (var != null) {
			var.modify(player, spell, amount);
		}
	}
	
	public double getValue(String variable, Player player, Spell spell) {
		Variable var = variables.get(variable);
		if (var != null) {
			return var.getValue(player, spell);
		} else {
			return 0;
		}
	}
	
	public void reset(String variable, Player player, Spell spell) {
		Variable var = variables.get(variable);
		if (var != null) {
			var.reset(player, spell);
		}
	}
	
}
