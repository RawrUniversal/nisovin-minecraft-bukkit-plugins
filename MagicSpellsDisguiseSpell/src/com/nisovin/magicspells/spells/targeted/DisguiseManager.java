package com.nisovin.magicspells.spells.targeted;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.EntityAgeable;
import net.minecraft.server.EntityBat;
import net.minecraft.server.EntityBlaze;
import net.minecraft.server.EntityCaveSpider;
import net.minecraft.server.EntityChicken;
import net.minecraft.server.EntityCow;
import net.minecraft.server.EntityCreeper;
import net.minecraft.server.EntityEnderman;
import net.minecraft.server.EntityGiantZombie;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMagmaCube;
import net.minecraft.server.EntityMushroomCow;
import net.minecraft.server.EntityOcelot;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySilverfish;
import net.minecraft.server.EntitySkeleton;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.EntitySpider;
import net.minecraft.server.EntitySquid;
import net.minecraft.server.EntityTracker;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.EntityWitch;
import net.minecraft.server.EntityWolf;
import net.minecraft.server.EntityZombie;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet24MobSpawn;
import net.minecraft.server.Packet29DestroyEntity;
import net.minecraft.server.Packet40EntityMetadata;
import net.minecraft.server.Packet5EntityEquipment;
import net.minecraft.server.World;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nisovin.magicspells.MagicSpells;

public class DisguiseManager implements Listener {

	private Set<DisguiseSpell> disguiseSpells = new HashSet<DisguiseSpell>();
	private Map<String, Disguise> disguises = new HashMap<String, Disguise>();

	private PacketListener packetListener = null;
	
	public DisguiseManager() {
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		packetListener = new PacketListener();
		protocolManager.addPacketListener(packetListener);
		
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
	
	public void addDisguise(Player player, Disguise disguise) {
		if (isDisguised(player)) {
			removeDisguise(player);
		}
		disguises.put(player.getName().toLowerCase(), disguise);
		applyDisguise(player, disguise);
	}
	
	public void removeDisguise(Player player) {
		Disguise disguise = disguises.remove(player.getName().toLowerCase());
		if (disguise != null) {
			clearDisguise(player);
			disguise.getSpell().undisguise(player);
		}
	}
	
	public boolean isDisguised(Player player) {
		return disguises.containsKey(player.getName().toLowerCase());
	}
	
	public Disguise getDisguise(Player player) {
		return disguises.get(player.getName().toLowerCase());
	}
	
	public void destroy() {
		HandlerList.unregisterAll(this);
		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.removePacketListener(packetListener);
		
		disguises.clear();
		disguiseSpells.clear();
	}
	
	private void applyDisguise(Player player, Disguise disguise) {
		for (Player p : player.getWorld().getPlayers()) {
			if (!p.equals(player)) {
				EntityLiving entity = getEntity(player, disguise);
				if (entity != null) {
					sendDestroyEntityPacket(p, player);
					sendMobSpawnPacket(p, player, disguise);
				}
			}
		}
	}
	
	private void clearDisguise(Player player) {
		for (Player p : player.getWorld().getPlayers()) {
			if (!p.equals(player)) {
				sendDestroyEntityPacket(p, player);
				if (!player.isDead()) {
					sendPlayerSpawnPacket(p, player);
				}
			}
		}
	}

	private EntityLiving getEntity(Player player, Disguise disguise) {
		EntityType entityType = disguise.getEntityType();
		boolean flag = disguise.getFlag();
		int var = disguise.getVar();
		Location location = player.getLocation();
		EntityLiving entity = null;
		World world = ((CraftWorld)location.getWorld()).getHandle();
		if (entityType == EntityType.ZOMBIE) {
			entity = new EntityZombie(world);
			if (flag) {
				((EntityZombie)entity).setBaby(true);
			}
			if (var == 1) {
				((EntityZombie)entity).setVillager(true);
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
			
		} else if (entityType == EntityType.CAVE_SPIDER) {
			entity = new EntityCaveSpider(world);
			
		} else if (entityType == EntityType.WOLF) {
			entity = new EntityWolf(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			
		} else if (entityType == EntityType.OCELOT) {
			entity = new EntityOcelot(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			((EntityOcelot)entity).setCatType(var);
			
		} else if (entityType == EntityType.BLAZE) {
			entity = new EntityBlaze(world);
			
		} else if (entityType == EntityType.GIANT) {
			entity = new EntityGiantZombie(world);
			
		} else if (entityType == EntityType.ENDERMAN) {
			entity = new EntityEnderman(world);
			
		} else if (entityType == EntityType.SILVERFISH) {
			entity = new EntitySilverfish(world);
			
		} else if (entityType == EntityType.WITCH) {
			entity = new EntityWitch(world);
			
		} else if (entityType == EntityType.VILLAGER) {
			entity = new EntityVillager(world);
			
			((EntityVillager)entity).setProfession(var);
		} else if (entityType == EntityType.PIG_ZOMBIE) {
			entity = new EntityPigZombie(world);
			
		} else if (entityType == EntityType.SLIME) {
			entity = new EntitySlime(world);
			entity.getDataWatcher().watch(16, Byte.valueOf((byte)2));
			
		} else if (entityType == EntityType.MAGMA_CUBE) {
			entity = new EntityMagmaCube(world);
			entity.getDataWatcher().watch(16, Byte.valueOf((byte)2));
			
		} else if (entityType == EntityType.BAT) {
			entity = new EntityBat(world);
			
		} else if (entityType == EntityType.CHICKEN) {
			entity = new EntityChicken(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			
		} else if (entityType == EntityType.COW) {
			entity = new EntityCow(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			
		} else if (entityType == EntityType.MUSHROOM_COW) {
			entity = new EntityMushroomCow(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			
		} else if (entityType == EntityType.PIG) {
			entity = new EntityPig(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			
		} else if (entityType == EntityType.SHEEP) {
			entity = new EntitySheep(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			((EntitySheep)entity).setColor(var);
			
		} else if (entityType == EntityType.SQUID) {
			entity = new EntitySquid(world);
			
		}
		
		if (entity != null) {
			entity.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
			return entity;
		} else {
			return null;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onArmSwing(PlayerAnimationEvent event) {
		final Player p = event.getPlayer();
		if (isDisguised(p)) {
			Disguise disguise = getDisguise(p);
			EntityType entityType = disguise.getEntityType();
			if (entityType == EntityType.IRON_GOLEM) {
				((CraftWorld)p.getWorld()).getHandle().broadcastEntityEffect(((CraftEntity)p).getHandle(), (byte) 4);
			} else if (entityType == EntityType.WITCH) {
				((CraftWorld)p.getWorld()).getHandle().broadcastEntityEffect(((CraftEntity)p).getHandle(), (byte) 15);
			} else if (entityType == EntityType.VILLAGER) {
				((CraftWorld)p.getWorld()).getHandle().broadcastEntityEffect(((CraftEntity)p).getHandle(), (byte) 13);
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
		Disguise disguise = getDisguise(event.getPlayer());
		if (disguise == null) return;
		EntityType entityType = disguise.getEntityType();
		if (entityType == EntityType.WOLF) {
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
		} else if (entityType == EntityType.ENDERMAN) {
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
		} else if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
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
		} else if (entityType == EntityType.SHEEP && event.isSneaking()) {
			Player p = event.getPlayer();
			p.playEffect(EntityEffect.SHEEP_EAT);
		}
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
	
	private void sendDestroyEntityPacket(Player viewer, Player disguised) {
		Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());		
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.netServerHandler.sendPacket(packet29);
	}
	
	private void sendMobSpawnPacket(Player viewer, Player disguised, Disguise disguise) {
		EntityLiving entity = getEntity(disguised, disguise);
		if (entity != null) {
			Packet24MobSpawn packet24 = new Packet24MobSpawn(entity);
			packet24.a = disguised.getEntityId();
			EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
			ep.netServerHandler.sendPacket(packet24);
			
			if (disguise.getEntityType() == EntityType.ZOMBIE || disguise.getEntityType() == EntityType.SKELETON) {
				ItemStack inHand = disguised.getItemInHand();
				if (inHand != null && inHand.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 0, ((CraftItemStack)inHand).getHandle());
					ep.netServerHandler.sendPacket(packet5);
				}
			}
		}
	}
	
	private void sendPlayerSpawnPacket(Player viewer, Player player) {
		Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn(((CraftPlayer)player).getHandle());		
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.netServerHandler.sendPacket(packet20);
	}
	
}
