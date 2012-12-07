package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;

import net.minecraft.server.EntityBlaze;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityEnderman;
import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMagmaCube;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EntityWither;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.World;

public class Disguise {

	private Player player;
	private EntityType entityType;
	private boolean baby;
	private boolean flag;
	
	private int taskId;
	
	public Disguise(Player player, EntityType entityType, boolean baby, boolean flag) {
		this.player = player;
		this.entityType = entityType;
		this.baby = baby;
		this.flag = flag;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public EntityLiving getEntity() {
		EntityLiving entity = null;
		World world = ((CraftWorld)player.getWorld()).getHandle();
		if (entityType == EntityType.ZOMBIE) {
			entity = new EntityZombie(world);
			if (baby) {
				((EntityZombie)entity).setBaby(true);
			}
		} else if (entityType == EntityType.SKELETON) {
			entity = new EntitySkeleton(world);
			if (flag) {
				((EntitySkeleton)entity).setSkeletonType(1);
			}
		} else if (entityType == EntityType.IRON_GOLEM) {
			entity = new EntityIronGolem(world);
		} else if (entityType == EntityType.CREEPER) {
			entity = new EntityCreeper(world);
		} else if (entityType == EntityType.SPIDER) {
			entity = new EntitySpider(world);
		} else if (entityType == EntityType.WOLF) {
			entity = new EntityWolf(world);
		} else if (entityType == EntityType.BLAZE) {
			entity = new EntityBlaze(world);
		} else if (entityType == EntityType.GIANT) {
			entity = new EntityGiantZombie(world);
		} else if (entityType == EntityType.ENDERMAN) {
			entity = new EntityEnderman(world);
		} else if (entityType == EntityType.WITHER) {
			entity = new EntityWither(world);
		} else if (entityType == EntityType.SLIME) {
			entity = new EntitySlime(world);
			entity.getDataWatcher().watch(16, Byte.valueOf((byte)2));
		} else if (entityType == EntityType.MAGMA_CUBE) {
			entity = new EntityMagmaCube(world);
			entity.getDataWatcher().watch(16, Byte.valueOf((byte)2));
		}
		if (entity != null) {
			Location loc = player.getLocation();
			entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			return entity;
		} else {
			return null;
		}
	}
	
	public void startDuration(int duration) {
		taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
			public void run() {
				DisguiseSpell.cancelDisguise(player);
			}
		}, duration);
	}
	
	public void cancelDuration() {
		if (taskId > 0) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = 0;
		}
	}
	
}
