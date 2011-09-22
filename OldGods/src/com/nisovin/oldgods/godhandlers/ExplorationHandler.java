package com.nisovin.oldgods.godhandlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.MobEffect;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class ExplorationHandler {

	public static void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == DamageCause.FALL) {
			event.setDamage(event.getDamage() / 2);
		}		
	}
	
	public static void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
		if (event.isSprinting()) {
			event.setCancelled(true);
			setMobEffect(event.getPlayer(), 1, 300, 1);
		} else {
			removeMobEffect(event.getPlayer(), 1);
		}
	}
	
	public static void onPlayerMove(PlayerMoveEvent event) {
		Player p = event.getPlayer();
		if (p.isSneaking() && event.getFrom().getY() == event.getTo().getY() && (event.getFrom().getX() != event.getTo().getX() || event.getFrom().getZ() != event.getTo().getZ())) {
			p.setVelocity(p.getLocation().getDirection().setY(0).normalize().multiply(1.7));
		}
	}
	
	public static void setMobEffect(LivingEntity entity, int type, int duration, int amplifier) {		
		((CraftLivingEntity)entity).getHandle().d(new MobEffect(type, duration, amplifier));
	}
	
	public static void removeMobEffect(LivingEntity entity, int type) {
		Method method;
		try {
			method = EntityLiving.class.getDeclaredMethod("c", MobEffect.class);
			method.setAccessible(true);
			method.invoke(((CraftLivingEntity)entity).getHandle(), new MobEffect(type, 0, 0));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
}
