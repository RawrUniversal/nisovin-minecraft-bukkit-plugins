package com.nisovin.magicspells.foodcasting;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.v1_5_R2.Packet14BlockDig;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.util.CastItem;
import com.nisovin.magicspells.util.Util;

public class FoodCasting extends JavaPlugin implements Listener {

	PacketListener packetListener;
	Map<CastItem, Spell> foodCastItems;
	Map<String, Long> eating;
	Map<String, Float> spellPower;
	
	@Override
	public void onEnable() {
		load();
	}
	
	@Override
	public void onDisable() {
		unload();
	}
	
	public void load() {
		foodCastItems = new HashMap<CastItem, Spell>();
		eating = new HashMap<String, Long>();
		spellPower = new HashMap<String, Float>();

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		packetListener = new PacketListener(this);
		protocolManager.addPacketListener(packetListener);
		
		getServer().getPluginManager().registerEvents(this, this);
		
		File configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			saveDefaultConfig();
		}
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		Set<String> keys = config.getKeys(false);
		for (String key : keys) {
			String val = config.getString(key);
			Spell spell = MagicSpells.getSpellByInternalName(key);
			ItemStack castItemStack = Util.getItemStackFromString(val);
			if (spell == null) {
				MagicSpells.error("Invalid food cast spell: " + key);
			} else if (castItemStack == null) {
				MagicSpells.error("Invalid food cast item: " + val);
			} else {
				CastItem castItem = new CastItem(castItemStack);
				foodCastItems.put(castItem, spell);
			}
		}
	}
	
	public void unload() {
		ProtocolLibrary.getProtocolManager().removePacketListener(packetListener);
		HandlerList.unregisterAll((Listener)this);
	}
	
	@EventHandler
	public void onMagicSpellsLoad(MagicSpellsLoadedEvent event) {
		unload();
		load();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		((Player)sender).sendMessage("Food: " + ((Player)sender).getFoodLevel());
		((Player)sender).setFoodLevel(4);
		return true;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (!event.hasItem()) {
			return;
		}
		
		if (event.getItem().getType().isEdible()) {
			eating.put(event.getPlayer().getName(), System.currentTimeMillis());
		}		
	}
	
	public void castWithPower(Player player, float power) {
		ItemStack item = player.getItemInHand();
		if (item == null || item.getType() == Material.AIR) return;
		CastItem castItem = new CastItem(item);
		Spell spell = foodCastItems.get(castItem);
		if (spell == null) return;
		spellPower.put(player.getName(), power);
		MagicSpells.debug(player.getName() + " foodcasting " + spell.getName() + " with power " + power);
		spell.cast(player);
	}
	
	@EventHandler
	public void onEat(final PlayerItemConsumeEvent event) {
		if (eating.containsKey(event.getPlayer().getName())) {
			eating.remove(event.getPlayer().getName());
			event.setCancelled(true);
			final int food = event.getPlayer().getFoodLevel();
			event.getPlayer().setFoodLevel(0);
			Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
				public void run() {
					castWithPower(event.getPlayer(), 1.0F);
					event.getPlayer().setFoodLevel(food);
				}
			});
		}
	}
	
	@EventHandler
	public void onSpellCast(SpellCastEvent event) {
		Float power = spellPower.remove(event.getCaster().getName());
		if (power != null) {
			event.increasePower(power.floatValue());
		}
	}
	
	class PacketListener extends PacketAdapter {
		
		Plugin plugin;
		
		public PacketListener(Plugin plugin) {
			super(plugin, ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL, 14);
			this.plugin = plugin;
		}
		
		@Override
		public void onPacketReceiving(final PacketEvent event) {
			if (eating.containsKey(event.getPlayer().getName())) {
				final long eatStart = eating.remove(event.getPlayer().getName());
				Packet14BlockDig packet = (Packet14BlockDig)event.getPacket().getHandle();
				if (packet.e == 5 && packet.a == 0 && packet.b == 0 && packet.c == 0 && packet.face == 255) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							long eatDuration = (System.currentTimeMillis() - eatStart);
							if (eatDuration > 300) {
								float power = eatDuration / 1800F;
								if (power > 0.9) power = 0.9F;
								castWithPower(event.getPlayer(), power);
							}
						}
					});
				}
			}
		}
		
	}
	
}
