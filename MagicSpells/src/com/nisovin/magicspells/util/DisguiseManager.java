package com.nisovin.magicspells.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.targeted.DisguiseSpell;

public abstract class DisguiseManager implements Listener {

	protected boolean hideArmor;
	
	protected Set<DisguiseSpell> disguiseSpells = new HashSet<DisguiseSpell>();
	protected Map<String, DisguiseSpell.Disguise> disguises = new HashMap<String, DisguiseSpell.Disguise>();
	protected Set<Integer> disguisedEntityIds = new HashSet<Integer>();
	protected Set<Integer> dragons = new HashSet<Integer>();
	protected Map<Integer, Integer> mounts = new HashMap<Integer, Integer>();

	protected ProtocolManager protocolManager;
	protected PacketAdapter packetListener = null;
	protected Random random = new Random();	

	public DisguiseManager(MagicConfig config) {
		this.hideArmor = config.getBoolean("general.disguise-spell-hide-armor", false);		
		protocolManager = ProtocolLibrary.getProtocolManager();		
		Bukkit.getPluginManager().registerEvents(this, MagicSpells.plugin);
	}
	
	public void registerSpell(DisguiseSpell spell) {
		disguiseSpells.add(spell);
	}
	
	public void unregisterSpell(DisguiseSpell spell) {
		disguiseSpells.remove(spell);
	}
	
	public int registeredSpellsCount() {
		return disguiseSpells.size();
	}
	
	public void addDisguise(Player player, DisguiseSpell.Disguise disguise) {
		if (isDisguised(player)) {
			removeDisguise(player);
		}
		disguises.put(player.getName().toLowerCase(), disguise);
		disguisedEntityIds.add(player.getEntityId());
		if (disguise.getEntityType() == EntityType.ENDER_DRAGON) {
			dragons.add(player.getEntityId());
		}
		applyDisguise(player, disguise);
	}
	
	public void removeDisguise(Player player) {
		removeDisguise(player, true);
	}
	
	public void removeDisguise(Player player, boolean sendPlayerPackets) {
		DisguiseSpell.Disguise disguise = disguises.remove(player.getName().toLowerCase());
		disguisedEntityIds.remove(player.getEntityId());
		dragons.remove(player.getEntityId());
		if (disguise != null) {
			clearDisguise(player, sendPlayerPackets);
			disguise.getSpell().undisguise(player);
		}
		mounts.remove(player.getEntityId());
	}
	
	public boolean isDisguised(Player player) {
		return disguises.containsKey(player.getName().toLowerCase());
	}
	
	public DisguiseSpell.Disguise getDisguise(Player player) {
		return disguises.get(player.getName().toLowerCase());
	}
	
	public void destroy() {
		HandlerList.unregisterAll(this);
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.removePacketListener(packetListener);
		
		disguises.clear();
		disguisedEntityIds.clear();
		dragons.clear();
		mounts.clear();
		disguiseSpells.clear();
	}
	
	private void applyDisguise(Player player, DisguiseSpell.Disguise disguise) {
		sendDestroyEntityPackets(player);
		sendDisguisedSpawnPackets(player, disguise);
	}
	
	private void clearDisguise(Player player, boolean sendPlayerPackets) {
		if (sendPlayerPackets) {
			sendDestroyEntityPackets(player);
		}
		if (mounts.containsKey(player.getEntityId())) {
			sendDestroyEntityPackets(player, mounts.remove(player.getEntityId()));
		}
		if (sendPlayerPackets && player.isValid()) {
			sendPlayerSpawnPackets(player);
		}
	}
	
	protected abstract void sendDestroyEntityPackets(Player disguised);
	
	protected abstract void sendDestroyEntityPackets(Player disguised, int entityId);
	
	protected abstract void sendDisguisedSpawnPackets(Player disguised, DisguiseSpell.Disguise disguise);
	
	protected abstract void sendPlayerSpawnPackets(Player player);
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		disguisedEntityIds.remove(event.getPlayer().getEntityId());
		dragons.remove(event.getPlayer().getEntityId());
		if (mounts.containsKey(event.getPlayer().getEntityId())) {
			sendDestroyEntityPackets(event.getPlayer(), mounts.remove(event.getPlayer().getEntityId()));
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (isDisguised(p)) {
			disguisedEntityIds.add(p.getEntityId());
			if (getDisguise(p).getEntityType() == EntityType.ENDER_DRAGON) {
				dragons.add(p.getEntityId());
			}
		}
	}
	
}
