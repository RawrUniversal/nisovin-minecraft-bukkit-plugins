package com.nisovin.magicspells.spells.targeted;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.kitteh.tag.PlayerReceiveNameTagEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;

public class DisguiseSpell extends TargetedEntitySpell {

	private static Map<String, Disguise> disguises = new HashMap<String, Disguise>();
	
	private int duration;
	private boolean toggle;
	
	public DisguiseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		duration = getConfigInt("duration", 200);
		toggle = getConfigBoolean("toggle", false);
		targetSelf = getConfigBoolean("target-self", true);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		Disguise oldDisguise = disguises.remove(player.getName().toLowerCase());
		if (oldDisguise != null && toggle) {			
			restore(player);
			return PostCastAction.ALREADY_HANDLED;
		}
		if (state == SpellCastState.NORMAL) {
			if (oldDisguise != null) {
				oldDisguise.cancelDuration();
			}
			Player target = getTargetPlayer(player);
			if (target != null) {
				disguise(target);
			} else {
				return noTarget(player);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	private void disguise(Player target) {
		Disguise disguise = new Disguise(target);
		disguises.put(target.getName().toLowerCase(), disguise);
		if (duration > 0) {
			disguise.startDuration(duration);
		}
		transform(target, disguise);
	}
	
	@Override
	public boolean castAtEntity(Player player, LivingEntity target, float power) {
		if (target instanceof Player) {
			disguise((Player)target);
			return true;
		}
		return false;
	}
	
	@EventHandler
	public void onSeePlayer(final PlayerReceiveNameTagEvent event) {
		String targetName = event.getNamedPlayer().getName().toLowerCase();
		
		final Disguise disguise = disguises.get(targetName);
		if (disguise != null) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
				public void run() {
					transform(event.getPlayer(), event.getNamedPlayer(), disguise);
				}
			}, 1);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onArmSwing(PlayerAnimationEvent event) {
		if (isDisguised(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	public static void cancelDisguise(Player player) {
		Disguise disguise = disguises.remove(player.getName().toLowerCase());
		if (disguise != null) {
			restore(player);
		}
	}
	
	@Override
	public void turnOff() {
		for (Disguise disguise : disguises.values()) {
			disguise.cancelDuration();
			restore(disguise.getPlayer());
		}
		disguises.clear();
	}
	
	private boolean isDisguised(Player player) {
		return disguises.containsKey(player.getName().toLowerCase());
	}
	
	private void transform(Player disguised, Disguise disguise) {
		for (Player p : disguised.getWorld().getPlayers()) {
			if (!p.equals(disguised)) {
				transform(p, disguised, disguise);
			}
		}
	}
	
	private void transform(Player viewer, Player disguised, Disguise disguise) {
		Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());
		Packet24MobSpawn packet24 = new Packet24MobSpawn(disguise.getEntity());
		packet24.a = disguised.getEntityId();
		
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.netServerHandler.sendPacket(packet29);
		ep.netServerHandler.sendPacket(packet24);
	}
	
	private static void restore(Player disguised) {
		for (Player p : disguised.getWorld().getPlayers()) {
			if (!p.equals(disguised)) {
				restore(p, disguised);
			}
		}
	}
	
	private static void restore(Player viewer, Player disguised) {
		Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());
		Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn(((CraftPlayer)disguised).getHandle());
		
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.netServerHandler.sendPacket(packet29);
		ep.netServerHandler.sendPacket(packet20);
	}

}
