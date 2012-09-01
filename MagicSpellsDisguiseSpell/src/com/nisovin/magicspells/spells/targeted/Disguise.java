package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;

import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityZombie;

public class Disguise {

	private Player player;
	private int taskId;
	
	public Disguise(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public EntityLiving getEntity() {
		EntityLiving entity = new EntityZombie(((CraftWorld)player.getWorld()).getHandle());
		Location loc = player.getLocation();
		entity.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
		return entity;
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
