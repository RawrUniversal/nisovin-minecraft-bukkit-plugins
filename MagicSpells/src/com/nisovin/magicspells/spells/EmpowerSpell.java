package com.nisovin.magicspells.spells;

import java.util.HashMap;

import org.bukkit.entity.Player;
import com.nisovin.magicspells.BuffSpell;
import com.nisovin.magicspells.events.MagicEventType;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.util.MagicConfig;

public class EmpowerSpell extends BuffSpell {

	private float extraPower;
	
	private HashMap<Player,Float> empowered;
	
	public EmpowerSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		extraPower = getConfigFloat("power-multiplier", 1.5F);
		
		empowered = new HashMap<Player,Float>();
		
		addListener(MagicEventType.SPELL_CAST);
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (empowered.containsKey(player)) {
			turnOff(player);
			return PostCastAction.ALREADY_HANDLED;
		} else if (state == SpellCastState.NORMAL) {
			float p = power * extraPower;
			empowered.put(player, p);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public void onSpellCast(SpellCastEvent event) {
		Player player = event.getCaster();
		if (empowered.containsKey(player)) {
			event.increasePower(empowered.get(player));
			addUseAndChargeCost(player);
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		if (empowered.containsKey(player)) {
			empowered.remove(player);
			sendMessage(player, strFade);
		}
	}

	@Override
	protected void turnOff() {
		empowered.clear();
	}

}
