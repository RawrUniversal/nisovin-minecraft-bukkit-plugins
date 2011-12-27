package com.nisovin.magicspells.spells.buff;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class SafefallSpell extends BuffSpell {

	private HashSet<String> safefallers;
	private boolean listening;
	
	public SafefallSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		safefallers = new HashSet<String>();
		listening = false;
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (safefallers.contains(player.getName())) {
			turnOff(player);
			return PostCastAction.ALREADY_HANDLED;
		} else if (state == SpellCastState.NORMAL) {
			safefallers.add(player.getName());
			startSpellDuration(player);
			if (!listening) {
				addListener(Event.Type.ENTITY_DAMAGE);
				listening = true;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL && !event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player)event.getEntity();
			if (safefallers.contains(player.getName())) {
				if (isExpired(player)) {
					turnOff(player);
				} else {
					addUse(player);
					boolean ok = chargeUseCost(player);
					if (ok) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		safefallers.remove(player.getName());
		sendMessage(player, strFade);
		//if (listening && safefallers.size() == 0) {
		//	removeListener(Event.Type.ENTITY_DAMAGE);
		//	listening = false;
		//}
	}
	
	@Override
	protected void turnOff() {
		
	}
	
}