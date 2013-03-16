package com.nisovin.muddersmilk;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MilkPlayerListener implements Listener {

	private MuddersMilk plugin;
	private Random random;
	
	public MilkPlayerListener(MuddersMilk plugin) {
		this.plugin = plugin;
		this.random = new Random();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@SuppressWarnings("deprecation")
	public void onItemConsume(PlayerItemConsumeEvent event) {
		final ItemStack item = event.getItem().clone();
		if (item.getTypeId() == plugin.itemId && item.getDurability() == plugin.itemData) {
			final Player player = event.getPlayer();
			
			// cancel event
			event.setCancelled(true);
			
			// handle consumed item
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					// remove item
					boolean emptyHand;
					if (item.getAmount() > 1) {
						item.setAmount(item.getAmount() - 1);
						player.setItemInHand(item);
						emptyHand = false;
					} else {
						player.setItemInHand(null);
						emptyHand = true;
					}			
					// add empty item
					if (plugin.itemEmptyId > 0 && !plugin.destroyBucketOnUse) {
						if (emptyHand) {
							player.setItemInHand(new ItemStack(plugin.itemEmptyId, 1, (short)plugin.itemEmptyData));
						} else {
							player.getInventory().addItem(new ItemStack(plugin.itemEmptyId, 1, (short)plugin.itemEmptyData));
						}
					}			
					// update inventory
					player.updateInventory();
				}
			});
			
			// increase drunk level
			int drunkLevel = plugin.moreDrunk(event.getPlayer());
			if (drunkLevel == plugin.tipsyLevel) {
				player.sendMessage(ChatColor.YELLOW + plugin.tipsyStr);
			} else if (drunkLevel == plugin.smashedLevel) {
				player.sendMessage(ChatColor.YELLOW + plugin.smashedStr);
			} else if (drunkLevel >= plugin.poisoningLevel) {
				player.damage(drunkLevel-(plugin.poisoningLevel-1));
				player.sendMessage(ChatColor.YELLOW + plugin.poisoningStr);
			}
			
			// vision blur
			if (plugin.visionBlurDurationPerLevel > 0) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, drunkLevel * plugin.visionBlurDurationPerLevel, 0), true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!event.isCancelled()) {
			int drunkLevel = plugin.getDrunkLevel(event.getPlayer());
			if (drunkLevel > 0) {
				String msg = event.getMessage();
				boolean changed = false;
				
				// slur "s"
				if (random.nextInt(100) < (plugin.chanceToSlurS + drunkLevel*plugin.chanceToSlurSPerLevel)) {
					msg = msg.replaceAll("s([^h])", "sh$1");
					changed = true;
				}
				
				// add "hic"
				if (random.nextInt(100) < plugin.chanceToHic + drunkLevel*plugin.chanceToHicPerLevel) {
					msg += "... hic!";
					changed = true;
				}
				
				if (changed) {
					event.setMessage(msg);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (plugin.soberOnDeath) {
			plugin.getDrunks().remove(event.getPlayer().getName());
		}
	}
	
}
