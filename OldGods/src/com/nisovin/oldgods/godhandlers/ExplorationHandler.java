package com.nisovin.oldgods.godhandlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.MobEffect;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.oldgods.OldGods;

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
	
	public static void pray(Player player, Block block, int amount) {
		int chance = player.hasPermission("oldgods.disciple.exploration") ? 50 : 4;
		if (OldGods.random() > chance) return;
		
		int quantity = 0;
		Material type = null;
		int r = OldGods.random(4);
		if (r==0) {
			type = Material.RAILS;
			quantity = 8;
		} else if (r==1) {
			type = Material.POWERED_MINECART;
			quantity = 4;
		} else if (r==2) {
			type = Material.MINECART;
			quantity = 1;
		} else if (r==3) {
			type = Material.BOAT;
			quantity = 1;
		}
		
		if (quantity > 0 && type != null) {
			Block b = block.getRelative(BlockFace.UP);
			for (int i = 0; i < quantity; i++) {
				b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(type,1));
			}
		}
	}
}
