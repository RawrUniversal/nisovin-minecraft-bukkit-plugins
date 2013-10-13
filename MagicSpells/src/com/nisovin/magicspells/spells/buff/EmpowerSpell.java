package com.nisovin.magicspells.spells.buff;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class EmpowerSpell extends BuffSpell {

	private float extraPower;
	
	private HashMap<Player, Float> empowered;
	
	public EmpowerSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		extraPower = getConfigFloat("power-multiplier", 1.5F);
		
		empowered = new HashMap<Player, Float>();
	}

	@Override
	public boolean castBuff(Player player, float power, String[] args) {
		float p = power * extraPower;
		empowered.put(player, p);
		return true;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onSpellCast(SpellCastEvent event) {
		Player player = event.getCaster();
		if (empowered.containsKey(player)) {
			event.increasePower(empowered.get(player));
			addUseAndChargeCost(player);
		}
	}
	
	@Override
	public void turnOffBuff(Player player) {
		empowered.remove(player);
	}

	@Override
	protected void turnOff() {
		empowered.clear();
	}

	@Override
	public boolean isActive(Player player) {
		return empowered.containsKey(player);
	}

}
