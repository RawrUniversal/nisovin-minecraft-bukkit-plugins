package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet40EntityMetadata;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.MagicConfig;

public class DisguiseSpell extends TargetedEntitySpell {

	private static Map<String, Disguise> disguises = new HashMap<String, Disguise>();
	
	private EntityType entityType;
	private boolean baby = false;
	private boolean flag = false;
	
	private int duration;
	private boolean toggle;
	
	public DisguiseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		String type = getConfigString("entity-type", "zombie");
		if (type.startsWith("baby ")) {
			baby = true;
			type = type.replace("baby ", "");
		}
		if (type.equalsIgnoreCase("wither skeleton")) {
			type = "skeleton";
			flag = true;
		}
		entityType = EntityType.fromName(type);
		duration = getConfigInt("duration", 0);
		toggle = getConfigBoolean("toggle", false);
		targetSelf = getConfigBoolean("target-self", true);
		
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(new PacketListener());
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
		Disguise disguise = new Disguise(target, entityType, baby, flag);
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
	
	class PacketListener extends PacketAdapter {
		
		public PacketListener() {
			super(MagicSpells.plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0x14);
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			if (event.getPacketID() == 0x14) {
				final Player player = event.getPlayer();
				final String name = event.getPacket().getStrings().getValues().get(0);
				final Disguise disguise = disguises.get(name.toLowerCase());
				if (disguise != null) {
					System.out.println("player spawn rewrite");
					event.setCancelled(true);
					Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
						public void run() {
							sendMobSpawnPacket(player, Bukkit.getPlayer(name), disguise);
						}
					}, 0);
				}
			}
		}
		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onArmSwing(PlayerAnimationEvent event) {
		final Player p = event.getPlayer();
		if (isDisguised(p)) {
			if (entityType == EntityType.IRON_GOLEM) {
				((CraftWorld)p.getWorld()).getHandle().broadcastEntityEffect(((CraftEntity)p).getHandle(), (byte) 4);
			} else if (entityType == EntityType.BLAZE || entityType == EntityType.SPIDER) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(16, Byte.valueOf((byte)0));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
					}
				}, 10);
			} else if (entityType == EntityType.CREEPER) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(17, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(17, Byte.valueOf((byte)0));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
					}
				}, 10);
			} else if (entityType == EntityType.WOLF) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)(p.isSneaking() ? 3 : 2)));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(16, Byte.valueOf((byte)(p.isSneaking() ? 1 : 0)));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
					}
				}, 10);
			} else if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)(p.isSneaking() ? 2 : 3)));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(16, Byte.valueOf((byte)(p.isSneaking() ? 1 : 2)));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
					}
				}, 10);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onSneak(PlayerToggleSneakEvent event) {
		if (entityType == EntityType.WOLF && isDisguised(event.getPlayer())) {
			Player p = event.getPlayer();
			if (event.isSneaking()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
			} else {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)0));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
			}
		} else if (entityType == EntityType.ENDERMAN && isDisguised(event.getPlayer())) {
			Player p = event.getPlayer();
			if (event.isSneaking()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(18, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
			} else {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(18, Byte.valueOf((byte)0));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
			}
		} else if ((entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) && isDisguised(event.getPlayer())) {
			Player p = event.getPlayer();
			if (event.isSneaking()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
			} else {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)2));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(p.getEntityId(), dw, true));
			}
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
		for (Disguise disguise : new ArrayList<Disguise>(disguises.values())) {
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
		System.out.println("transforming " + disguised.getName() + " for viewer " + viewer.getName());
		EntityLiving entity = disguise.getEntity();
		if (entity != null) {
			Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());
			Packet24MobSpawn packet24 = new Packet24MobSpawn(entity);
			packet24.a = disguised.getEntityId();
			
			EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
			ep.netServerHandler.sendPacket(packet29);
			ep.netServerHandler.sendPacket(packet24);
		}
	}
	
	private void sendMobSpawnPacket(Player viewer, Player disguised, Disguise disguise) {
		System.out.println("sending mob spawn about " + disguised.getName() + " for viewer " + viewer.getName());
		EntityLiving entity = disguise.getEntity();
		if (entity != null) {
			Packet24MobSpawn packet24 = new Packet24MobSpawn(entity);
			packet24.a = disguised.getEntityId();		
			EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
			ep.netServerHandler.sendPacket(packet24);
		}
	}
	
	private static void restore(Player disguised) {
		for (Player p : disguised.getWorld().getPlayers()) {
			if (!p.equals(disguised)) {
				disguises.remove(disguised.getName().toLowerCase());
				restore(p, disguised);
			}
		}
	}
	
	private static void restore(Player viewer, Player disguised) {
		System.out.println("restoring " + disguised.getName() + " for viewer " + viewer.getName());
		Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());
		Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn(((CraftPlayer)disguised).getHandle());
		
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.netServerHandler.sendPacket(packet29);
		ep.netServerHandler.sendPacket(packet20);
	}

}
