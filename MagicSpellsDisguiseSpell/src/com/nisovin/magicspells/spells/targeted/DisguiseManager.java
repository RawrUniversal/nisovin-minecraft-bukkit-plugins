package com.nisovin.magicspells.spells.targeted;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_5_R3.*;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_5_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_5_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.MagicConfig;

public class DisguiseManager implements Listener {

	private boolean hideArmor;
	
	private Set<DisguiseSpell> disguiseSpells = new HashSet<DisguiseSpell>();
	private Map<String, Disguise> disguises = new HashMap<String, Disguise>();
	private Set<Integer> disguisedEntityIds = new HashSet<Integer>();
	private Set<Integer> dragons = new HashSet<Integer>();

	private PacketListener packetListener = null;
	private Random random = new Random();
	
	public DisguiseManager(MagicConfig config) {
		this.hideArmor = config.getBoolean("general.disguise-spell-hide-armor", false);
		
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
		disguisedEntityIds.add(player.getEntityId());
		if (disguise.getEntityType() == EntityType.ENDER_DRAGON) {
			dragons.add(player.getEntityId());
		}
		applyDisguise(player, disguise);
	}
	
	public void removeDisguise(Player player) {
		Disguise disguise = disguises.remove(player.getName().toLowerCase());
		disguisedEntityIds.remove(player.getEntityId());
		dragons.remove(player.getEntityId());
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
		disguisedEntityIds.clear();
		dragons.clear();
		disguiseSpells.clear();
	}
	
	private void applyDisguise(Player player, Disguise disguise) {
		/*for (Player p : player.getWorld().getPlayers()) {
			if (!p.equals(player)) {
				EntityLiving entity = getEntity(player, disguise);
				if (entity != null) {
					sendDestroyEntityPacket(p, player);
					sendMobSpawnPacket(p, player, disguise, entity);
				}
			}
		}*/
		sendDestroyEntityPackets(player);
		sendDisguisedSpawnPackets(player, disguise);
	}
	
	private void clearDisguise(Player player) {
		/*for (Player p : player.getWorld().getPlayers()) {
			if (!p.equals(player)) {
				sendDestroyEntityPacket(p, player);
				if (player.isValid()) {
					sendPlayerSpawnPacket(p, player);
				}
			}
		}*/
		sendDestroyEntityPackets(player);
		if (player.isValid()) {
			sendPlayerSpawnPackets(player);
		}
	}

	private Entity getEntity(Player player, Disguise disguise) {
		EntityType entityType = disguise.getEntityType();
		boolean flag = disguise.getFlag();
		int var = disguise.getVar1();
		Location location = player.getLocation();
		Entity entity = null;
		float yOffset = 0;
		World world = ((CraftWorld)location.getWorld()).getHandle();
		if (entityType == EntityType.PLAYER) {
			entity = new EntityHuman(world) {				
				@Override
				public void sendMessage(String arg0) {
				}
				@Override
				public ChunkCoordinates b() {
					return null;
				}				
				@Override
				public boolean a(int arg0, String arg1) {
					return false;
				}
			};
			((EntityHuman)entity).name = disguise.getNameplateText();
			yOffset = -1.5F;
		} else if (entityType == EntityType.ZOMBIE) {
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
			
		} else if (entityType == EntityType.SNOWMAN) {
			entity = new EntitySnowman(world);
			
		} else if (entityType == EntityType.CREEPER) {
			entity = new EntityCreeper(world);
			if (flag) {
				((EntityCreeper)entity).setPowered(true);
			}
			
		} else if (entityType == EntityType.SPIDER) {
			entity = new EntitySpider(world);
			
		} else if (entityType == EntityType.CAVE_SPIDER) {
			entity = new EntityCaveSpider(world);
			
		} else if (entityType == EntityType.WOLF) {
			entity = new EntityWolf(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			if (var > 0) {
				((EntityWolf)entity).setTamed(true);
				((EntityWolf)entity).setOwnerName(player.getName());
				((EntityWolf)entity).setCollarColor(var);
			}
			
		} else if (entityType == EntityType.OCELOT) {
			entity = new EntityOcelot(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			if (var == -1) {
				((EntityOcelot)entity).setCatType(random.nextInt(4));
			} else if (var >= 0 && var < 4) {
				((EntityOcelot)entity).setCatType(var);
			}
			
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
			if (var == 1) {
				((EntityPig)entity).setSaddle(true);
			}
			
		} else if (entityType == EntityType.SHEEP) {
			entity = new EntitySheep(world);
			((EntityAgeable)entity).setAge(flag ? -24000 : 0);
			if (var == -1) {
				((EntitySheep)entity).setColor(random.nextInt(16));
			} else if (var >= 0 && var < 16) {
				((EntitySheep)entity).setColor(var);
			}
			
		} else if (entityType == EntityType.SQUID) {
			entity = new EntitySquid(world);
			
		} else if (entityType == EntityType.GHAST) {
			entity = new EntityGhast(world);
			
		} else if (entityType == EntityType.ENDER_DRAGON) {
			entity = new EntityEnderDragon(world);
						
		} else if (entityType == EntityType.FALLING_BLOCK) {
			int id = disguise.getVar1();
			int data = disguise.getVar2();
			entity = new EntityFallingBlock(world, 0, 0, 0, id > 0 ? id : 1, data > 15 ? 0 : data);
			
		} else if (entityType == EntityType.DROPPED_ITEM) {
			entity = new EntityItem(world);
			((EntityItem)entity).setItemStack(new net.minecraft.server.v1_5_R3.ItemStack(disguise.getVar1(), 1, disguise.getVar2()));
			
		}
		
		if (entity != null) {
			
			String nameplateText = disguise.getNameplateText();
			if (entity instanceof EntityLiving && nameplateText != null && !nameplateText.isEmpty()) {
				((EntityLiving)entity).setCustomName(nameplateText);
				((EntityLiving)entity).setCustomNameVisible(true);
			}
			
			entity.setPositionRotation(location.getX(), location.getY() + yOffset, location.getZ(), location.getYaw(), location.getPitch());
			
			return entity;
			
		} else {
			return null;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onArmSwing(PlayerAnimationEvent event) {
		final Player p = event.getPlayer();
		final int entityId = -p.getEntityId();
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
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(16, Byte.valueOf((byte)0));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
					}
				}, 10);
			} else if (entityType == EntityType.CREEPER && !disguise.getFlag()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(17, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(17, Byte.valueOf((byte)0));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
					}
				}, 10);
			} else if (entityType == EntityType.WOLF) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)(p.isSneaking() ? 3 : 2)));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(16, Byte.valueOf((byte)(p.isSneaking() ? 1 : 0)));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
					}
				}, 10);
			} else if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)(p.isSneaking() ? 2 : 3)));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
				Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						dw.watch(16, Byte.valueOf((byte)(p.isSneaking() ? 1 : 2)));
						tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
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
		Player p = event.getPlayer();
		int entityId = -p.getEntityId();
		if (entityType == EntityType.WOLF) {
			if (event.isSneaking()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
			} else {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)0));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
			}
		} else if (entityType == EntityType.ENDERMAN) {
			if (event.isSneaking()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(18, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
			} else {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(18, Byte.valueOf((byte)0));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
			}
		} else if (entityType == EntityType.SLIME || entityType == EntityType.MAGMA_CUBE) {
			if (event.isSneaking()) {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)1));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
			} else {
				final DataWatcher dw = new DataWatcher();
				final EntityTracker tracker = ((CraftWorld)p.getWorld()).getHandle().tracker;
				dw.a(0, Byte.valueOf((byte) 0));
				dw.a(1, Short.valueOf((short) 300));
				dw.a(16, Byte.valueOf((byte)2));
				tracker.a(((CraftPlayer)p).getHandle(), new Packet40EntityMetadata(entityId, dw, true));
			}
		} else if (entityType == EntityType.SHEEP && event.isSneaking()) {
			p.playEffect(EntityEffect.SHEEP_EAT);
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		disguisedEntityIds.remove(event.getPlayer().getEntityId());
		dragons.remove(event.getPlayer().getEntityId());
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
		
	class PacketListener extends PacketAdapter {
		
		public PacketListener() {
			super(MagicSpells.plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0x14, 0x28, 0x5);
		}
		
		@Override
		public void onPacketSending(PacketEvent event) {
			if (event.getPacketID() == 0x14) {
				Packet20NamedEntitySpawn packet = (Packet20NamedEntitySpawn)event.getPacket().getHandle();
				if (packet.a < 0) {
					packet.a *= -1;
				} else {
					final Player player = event.getPlayer();
					final String name = event.getPacket().getStrings().getValues().get(0);
					final Disguise disguise = disguises.get(name.toLowerCase());
					if (player != null && disguise != null) {
						event.setCancelled(true);
						Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
							public void run() {
								sendDisguisedSpawnPacket(player, Bukkit.getPlayer(name), disguise, null);
							}
						}, 0);
					}
				}
			} else if (hideArmor && event.getPacketID() == 0x5) {
				Packet5EntityEquipment packet = (Packet5EntityEquipment)event.getPacket().getHandle();
				if (packet.b > 0 && disguisedEntityIds.contains(packet.a)) {
					event.setCancelled(true);
				}
			/*} else if (event.getPacketID() == 0x20) {
				Packet32EntityLook packet = (Packet32EntityLook)event.getPacket().getHandle();
				if (dragons.contains(packet.a)) {
					int dir = packet.e + 128;
					if (dir > 127) dir -= 256;
					packet.e = (byte)dir;
				}
			} else if (event.getPacketID() == 0x21) {
				Packet33RelEntityMoveLook packet = (Packet33RelEntityMoveLook)event.getPacket().getHandle();
				if (dragons.contains(packet.a)) {
					int dir = packet.e + 128;
					if (dir > 127) dir -= 256;
					packet.e = (byte)dir;
				}
			} else if (event.getPacketID() == 0x22) {
				Packet34EntityTeleport packet = (Packet34EntityTeleport)event.getPacket().getHandle();
				if (dragons.contains(packet.a)) {
					int dir = packet.e + 128;
					if (dir > 127) dir -= 256;
					packet.e = (byte)dir;
				}
			} else if (event.getPacketID() == 0x23) {
				Packet35EntityHeadRotation packet = (Packet35EntityHeadRotation)event.getPacket().getHandle();
				if (dragons.contains(packet.a)) {
					int dir = packet.b + 128;
					if (dir > 127) dir -= 256;
					packet.b = (byte)dir;
				}*/
			} else if (event.getPacketID() == 0x28) {
				Packet40EntityMetadata packet = (Packet40EntityMetadata)event.getPacket().getHandle();
				if (packet.a < 0) {
					packet.a *= -1;
				} else if (disguisedEntityIds.contains(packet.a)) {
					event.setCancelled(true);
				}
			}
		}
		
	}
	
	/*private void sendDestroyEntityPacket(Player viewer, Player disguised) {
		Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());		
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.playerConnection.sendPacket(packet29);
	}*/
	
	private void sendDestroyEntityPackets(Player disguised) {
		Packet29DestroyEntity packet29 = new Packet29DestroyEntity(disguised.getEntityId());
		final EntityTracker tracker = ((CraftWorld)disguised.getWorld()).getHandle().tracker;
		tracker.a(((CraftPlayer)disguised).getHandle(), packet29);
	}
	
	private void sendDisguisedSpawnPacket(Player viewer, Player disguised, Disguise disguise, Entity entity) {
		if (entity == null) entity = getEntity(disguised, disguise);
		if (entity != null) {
			if (entity instanceof EntityHuman) {
				Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn((EntityHuman)entity);
				packet20.a = -disguised.getEntityId();
				EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
				ep.playerConnection.sendPacket(packet20);
			} else if (entity instanceof EntityLiving) {
				Packet24MobSpawn packet24 = new Packet24MobSpawn((EntityLiving)entity);
				packet24.a = disguised.getEntityId();
				if (dragons.contains(disguised.getEntityId())) {
					int dir = packet24.i + 128;
					if (dir > 127) dir -= 256;
					packet24.i = (byte)dir;
					packet24.k = (byte)dir;
				}
				EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
				ep.playerConnection.sendPacket(packet24);
				
				if (disguise.getEntityType() == EntityType.ZOMBIE || disguise.getEntityType() == EntityType.SKELETON) {
					ItemStack inHand = disguised.getItemInHand();
					if (inHand != null && inHand.getType() != Material.AIR) {
						Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 0, CraftItemStack.asNMSCopy(inHand));
						ep.playerConnection.sendPacket(packet5);
					}
				}
			} else {
				Packet23VehicleSpawn packet23 = new Packet23VehicleSpawn(entity, entity.getBukkitEntity().getType().getTypeId());
				packet23.a = disguised.getEntityId();
				EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
				ep.playerConnection.sendPacket(packet23);
			}
		}
	}
	
	private void sendDisguisedSpawnPackets(Player disguised, Disguise disguise) {
		Entity entity = getEntity(disguised, disguise);
		if (entity != null) {
			if (entity instanceof EntityHuman) {
				Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn((EntityHuman)entity);
				packet20.a = -disguised.getEntityId();
				final EntityTracker tracker = ((CraftWorld)disguised.getWorld()).getHandle().tracker;
				tracker.a(((CraftPlayer)disguised).getHandle(), packet20);
				
				ItemStack inHand = disguised.getItemInHand();
				if (inHand != null && inHand.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 0, CraftItemStack.asNMSCopy(inHand));
					tracker.a(((CraftPlayer)disguised).getHandle(), packet5);
				}
				
				ItemStack helmet = disguised.getInventory().getHelmet();
				if (helmet != null && helmet.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 4, CraftItemStack.asNMSCopy(helmet));
					tracker.a(((CraftPlayer)disguised).getHandle(), packet5);
				}
				
				ItemStack chestplate = disguised.getInventory().getChestplate();
				if (chestplate != null && chestplate.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 3, CraftItemStack.asNMSCopy(chestplate));
					tracker.a(((CraftPlayer)disguised).getHandle(), packet5);
				}
				
				ItemStack leggings = disguised.getInventory().getLeggings();
				if (leggings != null && leggings.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 2, CraftItemStack.asNMSCopy(leggings));
					tracker.a(((CraftPlayer)disguised).getHandle(), packet5);
				}
				
				ItemStack boots = disguised.getInventory().getBoots();
				if (boots != null && boots.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 1, CraftItemStack.asNMSCopy(boots));
					tracker.a(((CraftPlayer)disguised).getHandle(), packet5);
				}
			} else if (entity instanceof EntityLiving) {
				Packet24MobSpawn packet24 = new Packet24MobSpawn((EntityLiving)entity);
				packet24.a = disguised.getEntityId();
				if (dragons.contains(disguised.getEntityId())) {
					int dir = packet24.i + 128;
					if (dir > 127) dir -= 256;
					packet24.i = (byte)dir;
					packet24.k = (byte)dir;
				}
				final EntityTracker tracker = ((CraftWorld)disguised.getWorld()).getHandle().tracker;
				tracker.a(((CraftPlayer)disguised).getHandle(), packet24);
				
				if (disguise.getEntityType() == EntityType.ZOMBIE || disguise.getEntityType() == EntityType.SKELETON) {
					ItemStack inHand = disguised.getItemInHand();
					if (inHand != null && inHand.getType() != Material.AIR) {
						Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 0, CraftItemStack.asNMSCopy(inHand));
						tracker.a(((CraftPlayer)disguised).getHandle(), packet5);
					}
				}
			} else {
				Packet23VehicleSpawn packet23 = new Packet23VehicleSpawn(entity, entity.getBukkitEntity().getType().getTypeId());
				packet23.a = disguised.getEntityId();
				final EntityTracker tracker = ((CraftWorld)disguised.getWorld()).getHandle().tracker;
				tracker.a(((CraftPlayer)disguised).getHandle(), packet23);
			}
		}
	}
	
	/*private void sendPlayerSpawnPacket(Player viewer, Player player) {
		Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn(((CraftPlayer)player).getHandle());
		EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
		ep.playerConnection.sendPacket(packet20);
	}*/
	
	private void sendPlayerSpawnPackets(Player player) {
		Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn(((CraftPlayer)player).getHandle());
		final EntityTracker tracker = ((CraftWorld)player.getWorld()).getHandle().tracker;
		tracker.a(((CraftPlayer)player).getHandle(), packet20);
	}
	
}
