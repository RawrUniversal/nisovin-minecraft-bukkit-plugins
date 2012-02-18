package com.nisovin.muddersmilk;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MilkPlayerListener implements Listener {

	private MuddersMilk plugin;
	private Random random;
	
	private String justMilked = "";
	
	public MilkPlayerListener(MuddersMilk plugin) {
		this.plugin = plugin;
		this.random = new Random();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().getItemInHand().getType() == Material.MILK_BUCKET) {
			// don't drink if we just milked a cow
			if (event.getPlayer().getName().equals(justMilked)) {
				justMilked = "";
				return;
			}
			
			// destroy bucket if necessary
			if (plugin.destroyBucketOnUse) {
				event.getPlayer().setItemInHand(null);
			} else {
				event.getPlayer().setItemInHand(new ItemStack(Material.BUCKET, 1));				
			}
			
			// increase drunk level
			int drunkLevel = plugin.moreDrunk(event.getPlayer());
			if (drunkLevel == plugin.tipsyLevel) {
				event.getPlayer().sendMessage(ChatColor.YELLOW + plugin.tipsyStr);
			} else if (drunkLevel == plugin.smashedLevel) {
				event.getPlayer().sendMessage(ChatColor.YELLOW + plugin.smashedStr);
			} else if (drunkLevel >= plugin.poisoningLevel) {
				event.getPlayer().damage(drunkLevel-(plugin.poisoningLevel-1));
				event.getPlayer().sendMessage(ChatColor.YELLOW + plugin.poisoningStr);
			}
			
			// vision blur
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, drunkLevel * plugin.visionBlurDurationPerLevel, 0), true);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Cow && event.getPlayer().getItemInHand().getType() == Material.BUCKET) {
			justMilked = event.getPlayer().getName();
		}
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerChat(PlayerChatEvent event) {
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
