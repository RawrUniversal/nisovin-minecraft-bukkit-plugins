package com.nisovin.magicspells.spells.buff;

import java.lang.reflect.Field;
import java.util.HashMap;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffect;
import net.minecraft.server.Packet42RemoveMobEffect;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import com.nisovin.magicspells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class HasteSpell extends BuffSpell {

	private int strength;
	private int boostDuration;
	
	private HashMap<Player,Integer> hasted;
	
	public HasteSpell(MagicConfig config, String spellName) {
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
			super.turnOff(player);
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
		((CraftLivingEntity)entity).getHandle().addEffect(new MobEffect(type, duration, amplifier));
	}
	
	@SuppressWarnings("rawtypes")
	public static void removeMobEffect(LivingEntity entity, int type) {
		try {
			if (entity instanceof Player) {
				EntityPlayer player = ((CraftPlayer)entity).getHandle();
				player.netServerHandler.sendPacket(new Packet42RemoveMobEffect(player.id, new MobEffect(type, 0, 0)));
			}
			Field field = EntityLiving.class.getDeclaredField("effects");
			field.setAccessible(true);
			HashMap effects = (HashMap)field.get(((CraftLivingEntity)entity).getHandle());
			effects.remove(type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
