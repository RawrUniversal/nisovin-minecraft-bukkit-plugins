package com.nisovin.magicspells.spells;

import java.lang.reflect.Method;
import java.util.HashMap;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.MobEffect;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.magicspells.BuffSpell;

public class HasteSpell extends BuffSpell {

	private int strength;
	private int boostDuration;
	
	private HashMap<Player,Integer> hasted;
	
	public HasteSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strength = getConfigInt("effect-strength", 3);
		boostDuration = getConfigInt("boost-duration", 300);
		
		hasted = new HashMap<Player,Integer>();
		
		addListener(Event.Type.PLAYER_TOGGLE_SPRINT);
	}

	@Override
	protected PostCastAction castSpell(final Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			hasted.put(player, Math.round(strength*power));
			startSpellDuration(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		Player player = event.getPlayer();
		if (hasted.containsKey(player)) {
			if (isExpired(player)) {
				turnOff(player);
			} else if (event.isSprinting()) {
				event.setCancelled(true);
				setMobEffect(event.getPlayer(), 1, boostDuration, hasted.get(player));
				addUseAndChargeCost(player);
			} else {
				removeMobEffect(event.getPlayer(), 1);
			}
		}
	}

	@Override
	protected void turnOff(Player player) {
		if (hasted.containsKey(player)) {
			hasted.remove(player);
			removeMobEffect(player, 1);
			sendMessage(player, strFade);
		}
	}
	
	@Override
	protected void turnOff() {
		hasted.clear();
	}
	
	public void setMobEffect(LivingEntity entity, int type, int duration, int amplifier) {		
		((CraftLivingEntity)entity).getHandle().d(new MobEffect(type, duration, amplifier));
	}
	
	public void removeMobEffect(LivingEntity entity, int type) {
		Method method;
		try {
			method = EntityLiving.class.getDeclaredMethod("c", MobEffect.class);
			method.setAccessible(true);
			method.invoke(((CraftLivingEntity)entity).getHandle(), new MobEffect(type, 0, 0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}