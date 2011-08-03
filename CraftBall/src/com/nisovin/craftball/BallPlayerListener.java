package com.nisovin.craftball;

import net.minecraft.server.EntityItem;

import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.util.Vector;

public class BallPlayerListener extends PlayerListener {

	CraftBall plugin;
		
	public BallPlayerListener(CraftBall plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_DROP_ITEM, this, Event.Priority.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, this, Event.Priority.Normal, plugin);
	}
	
	@Override
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		Player player = event.getPlayer();
		for (Field field : plugin.fields) {
			if (field.enableKick && field.inField(item)) {
				Vector v = item.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(field.hKickPower);
				v.setY(field.vKickPower);
				item.setVelocity(v);
				if (field.fire) { 
					item.setFireTicks(6000);
				}
				event.setCancelled(true);
				return;
			} else if (field.enableBat && 
					field.batItem.getTypeId() == player.getItemInHand().getTypeId() && 
					field.batters.containsKey(player.getName()) && field.batters.get(player.getName()) + field.batDelay > System.currentTimeMillis() && 
					field.inField(item)) {
				Vector v = player.getLocation().getDirection().normalize().multiply(field.batPower);
				item.setVelocity(v);
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		Item item = event.getItemDrop();
		for (Field field : plugin.fields) {
			if (field.enableThrow && field.inField(item)) {
				((EntityItem)((CraftItem)item).getHandle()).pickupDelay = field.pickupDelay;
				item.setVelocity(event.getPlayer().getLocation().getDirection().normalize().multiply(field.throwPower));
				if (field.fire) { 
					item.setFireTicks(6000);
				}
				return;
			}
		}
	}
	
	@Override
	public void onPlayerAnimation(PlayerAnimationEvent event) {
		if (event.getAnimationType() == PlayerAnimationType.ARM_SWING) {
			Player player = event.getPlayer();
			for (Field field : plugin.fields) {
				if (field.enableBat && player.getItemInHand().getTypeId() == field.batItem.getTypeId() && field.inField(player)) {
					System.out.println("batted");
					field.batters.put(player.getName(), System.currentTimeMillis());
				}
			}
		}
	}

}
