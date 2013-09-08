package com.nisovin.magicspells.volatilecode;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.server.v1_6_R2.*;

import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.CraftServer;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftFallingSand;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftTNTPrimed;
import org.bukkit.craftbukkit.v1_6_R2.inventory.CraftItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.targeted.DisguiseSpell;
import com.nisovin.magicspells.util.DisguiseManager;
import com.nisovin.magicspells.util.MagicConfig;

public class VolatileCodeEnabled_1_6_R2 implements VolatileCodeHandle {

	
	private static NBTTagCompound getTag(ItemStack item) {
		if (item instanceof CraftItemStack) {
			try {
				Field field = CraftItemStack.class.getDeclaredField("handle");
				field.setAccessible(true);
				return ((net.minecraft.server.v1_6_R2.ItemStack)field.get(item)).tag;
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	private static ItemStack setTag(ItemStack item, NBTTagCompound tag) {
		CraftItemStack craftItem = null;
		if (item instanceof CraftItemStack) {
			craftItem = (CraftItemStack)item;
		} else {
			craftItem = CraftItemStack.asCraftCopy(item);
		}
		
		net.minecraft.server.v1_6_R2.ItemStack nmsItem = null;
		try {
			Field field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			nmsItem = ((net.minecraft.server.v1_6_R2.ItemStack)field.get(item));
		} catch (Exception e) {
		}
		if (nmsItem == null) {
			nmsItem = CraftItemStack.asNMSCopy(craftItem);
		}
		
		nmsItem.tag = tag;
		try {
			Field field = CraftItemStack.class.getDeclaredField("handle");
			field.setAccessible(true);
			field.set(craftItem, nmsItem);
		} catch (Exception e) {
		}
		
		return craftItem;
	}
	
	public VolatileCodeEnabled_1_6_R2() {
		try {
			packet63Fields[0] = Packet63WorldParticles.class.getDeclaredField("a");
			packet63Fields[1] = Packet63WorldParticles.class.getDeclaredField("b");
			packet63Fields[2] = Packet63WorldParticles.class.getDeclaredField("c");
			packet63Fields[3] = Packet63WorldParticles.class.getDeclaredField("d");
			packet63Fields[4] = Packet63WorldParticles.class.getDeclaredField("e");
			packet63Fields[5] = Packet63WorldParticles.class.getDeclaredField("f");
			packet63Fields[6] = Packet63WorldParticles.class.getDeclaredField("g");
			packet63Fields[7] = Packet63WorldParticles.class.getDeclaredField("h");
			packet63Fields[8] = Packet63WorldParticles.class.getDeclaredField("i");
			for (int i = 0; i <= 8; i++) {
				packet63Fields[i].setAccessible(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addPotionGraphicalEffect(LivingEntity entity, int color, int duration) {
		final EntityLiving el = ((CraftLivingEntity)entity).getHandle();
		final DataWatcher dw = el.getDataWatcher();
		dw.watch(7, Integer.valueOf(color));
		
		if (duration > 0) {
			MagicSpells.scheduleDelayedTask(new Runnable() {
				public void run() {
					int c = 0;
					if (!el.effects.isEmpty()) {
						c = net.minecraft.server.v1_6_R2.PotionBrewer.a(el.effects.values());
					}
					dw.watch(7, Integer.valueOf(c));
				}
			}, duration);
		}
	}

	@Override
	public void entityPathTo(LivingEntity creature, LivingEntity target) {
		EntityCreature entity = ((CraftCreature)creature).getHandle();
		entity.pathEntity = entity.world.findPath(entity, ((CraftLivingEntity)target).getHandle(), 16.0F, true, false, false, false);
	}

	@Override
	public void sendFakeSlotUpdate(Player player, int slot, ItemStack item) {
		net.minecraft.server.v1_6_R2.ItemStack nmsItem;
		if (item != null) {
			nmsItem = CraftItemStack.asNMSCopy(item);
		} else {
			nmsItem = null;
		}
		Packet103SetSlot packet = new Packet103SetSlot(0, (short)slot+36, nmsItem);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public void toggleLeverOrButton(Block block) {
		net.minecraft.server.v1_6_R2.Block.byId[block.getType().getId()].interact(((CraftWorld)block.getWorld()).getHandle(), block.getX(), block.getY(), block.getZ(), null, 0, 0, 0, 0);
	}

	@Override
	public void pressPressurePlate(Block block) {
		block.setData((byte) (block.getData() ^ 0x1));
		net.minecraft.server.v1_6_R2.World w = ((CraftWorld)block.getWorld()).getHandle();
		w.applyPhysics(block.getX(), block.getY(), block.getZ(), block.getType().getId());
		w.applyPhysics(block.getX(), block.getY()-1, block.getZ(), block.getType().getId());
	}

	@Override
	public boolean simulateTnt(Location target, LivingEntity source, float explosionSize, boolean fire) {
        EntityTNTPrimed e = new EntityTNTPrimed(((CraftWorld)target.getWorld()).getHandle(), target.getX(), target.getY(), target.getZ(), ((CraftLivingEntity)source).getHandle());
        CraftTNTPrimed c = new CraftTNTPrimed((CraftServer)Bukkit.getServer(), e);
        ExplosionPrimeEvent event = new ExplosionPrimeEvent(c, explosionSize, fire);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event.isCancelled();
	}

	@Override
	public boolean createExplosionByPlayer(Player player, Location location, float size, boolean fire, boolean breakBlocks) {
		return !((CraftWorld)location.getWorld()).getHandle().createExplosion(((CraftPlayer)player).getHandle(), location.getX(), location.getY(), location.getZ(), size, fire, breakBlocks).wasCanceled;
	}

	@Override
	public void playExplosionEffect(Location location, float size) {
		@SuppressWarnings("rawtypes")
		Packet60Explosion packet = new Packet60Explosion(location.getX(), location.getY(), location.getZ(), size, new ArrayList(), null);
		for (Player player : location.getWorld().getPlayers()) {
			if (player.getLocation().distanceSquared(location) < 50 * 50) {
				((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

	@Override
	public void setExperienceBar(Player player, int level, float percent) {
		Packet43SetExperience packet = new Packet43SetExperience(percent, player.getTotalExperience(), level);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public Fireball shootSmallFireball(Player player) {
		net.minecraft.server.v1_6_R2.World w = ((CraftWorld)player.getWorld()).getHandle();
		Location playerLoc = player.getLocation();
		Vector loc = player.getEyeLocation().toVector().add(player.getLocation().getDirection().multiply(10));
		
		double d0 = loc.getX() - playerLoc.getX();
        double d1 = loc.getY() - (playerLoc.getY() + 1.5);
        double d2 = loc.getZ() - playerLoc.getZ();
		EntitySmallFireball entitysmallfireball = new EntitySmallFireball(w, ((CraftPlayer)player).getHandle(), d0, d1, d2);

        entitysmallfireball.locY = playerLoc.getY() + 1.5;
        w.addEntity(entitysmallfireball);
        
        return (Fireball)entitysmallfireball.getBukkitEntity();
	}

	@Override
	public void setTarget(LivingEntity entity, LivingEntity target) {
		if (entity instanceof Creature) {
			((Creature)entity).setTarget(target);
		}
		((EntityInsentient)((CraftLivingEntity)entity).getHandle()).setGoalTarget(((CraftLivingEntity)target).getHandle());
	}

	@Override
	public void playSound(Location location, String sound, float volume, float pitch) {
		((CraftWorld)location.getWorld()).getHandle().makeSound(location.getX(), location.getY(), location.getZ(), sound, volume, pitch);
	}

	@Override
	public void playSound(Player player, String sound, float volume, float pitch) {
		Location loc = player.getLocation();
		Packet62NamedSoundEffect packet = new Packet62NamedSoundEffect(sound, loc.getX(), loc.getY(), loc.getZ(), volume, pitch);
		((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
	}

	@Override
	public ItemStack addFakeEnchantment(ItemStack item) {
		if (!(item instanceof CraftItemStack)) {
			item = CraftItemStack.asCraftCopy(item);
		}
		NBTTagCompound tag = getTag(item);		
		if (tag == null) {
			tag = new NBTTagCompound();
		}
		if (!tag.hasKey("ench")) {
			tag.set("ench", new NBTTagList("ench"));
		}		
		return setTag(item, tag);
	}

	@Override
	public void setFallingBlockHurtEntities(FallingBlock block, float damage, int max) {
		EntityFallingBlock efb = ((CraftFallingSand)block).getHandle();
		try {
			Field field = EntityFallingBlock.class.getDeclaredField("hurtEntities");
			field.setAccessible(true);
			field.setBoolean(efb, true);
			
			field = EntityFallingBlock.class.getDeclaredField("fallHurtAmount");
			field.setAccessible(true);
			field.setFloat(efb, damage);
			
			field = EntityFallingBlock.class.getDeclaredField("fallHurtMax");
			field.setAccessible(true);
			field.setInt(efb, max);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void playEntityAnimation(final Location location, final EntityType entityType, final int animationId, boolean instant) {
		final EntityLiving entity;
		if (entityType == EntityType.VILLAGER) {
			entity = new EntityVillager(((CraftWorld)location.getWorld()).getHandle());
		} else if (entityType == EntityType.WITCH) {
			entity = new EntityWitch(((CraftWorld)location.getWorld()).getHandle());
		} else if (entityType == EntityType.OCELOT) {
			entity = new EntityOcelot(((CraftWorld)location.getWorld()).getHandle());
		} else {
			entity = null;
		}
		if (entity == null) return;
		
		entity.setPosition(location.getX(), instant ? location.getY() : -5, location.getZ());
		((CraftWorld)location.getWorld()).getHandle().addEntity(entity);
		entity.addEffect(new MobEffect(14, 40));
		if (instant) {
			((CraftWorld)location.getWorld()).getHandle().broadcastEntityEffect(entity, (byte)animationId);
			entity.getBukkitEntity().remove();
		} else {
			entity.setPosition(location.getX(), location.getY(), location.getZ());
			MagicSpells.scheduleDelayedTask(new Runnable() {
				public void run() {
					((CraftWorld)location.getWorld()).getHandle().broadcastEntityEffect(entity, (byte)animationId);
					entity.getBukkitEntity().remove();
				}
			}, 8);
		}
	}

	@Override
	public void createFireworksExplosion(Location location, boolean flicker, boolean trail, int type, int[] colors, int[] fadeColors, int flightDuration) {
		// create item
		net.minecraft.server.v1_6_R2.ItemStack item = new net.minecraft.server.v1_6_R2.ItemStack(401, 1, 0);
		
		// get tag
		NBTTagCompound tag = item.tag;
		if (tag == null) {
			tag = new NBTTagCompound();
		}
		
		// create explosion tag
		NBTTagCompound explTag = new NBTTagCompound("Explosion");
		explTag.setByte("Flicker", flicker ? (byte)1 : (byte)0);
		explTag.setByte("Trail", trail ? (byte)1 : (byte)0);
		explTag.setByte("Type", (byte)type);
		explTag.setIntArray("Colors", colors);
		explTag.setIntArray("FadeColors", fadeColors);
		
		// create fireworks tag
		NBTTagCompound fwTag = new NBTTagCompound("Fireworks");
		fwTag.setByte("Flight", (byte)flightDuration);
		NBTTagList explList = new NBTTagList("Explosions");
		explList.add(explTag);
		fwTag.set("Explosions", explList);
		tag.setCompound("Fireworks", fwTag);
		
		// set tag
		item.tag = tag;
		
		// create fireworks entity
		EntityFireworks fireworks = new EntityFireworks(((CraftWorld)location.getWorld()).getHandle(), location.getX(), location.getY(), location.getZ(), item);
		((CraftWorld)location.getWorld()).getHandle().addEntity(fireworks);
		
		// cause explosion
		if (flightDuration == 0) {
			((CraftWorld)location.getWorld()).getHandle().broadcastEntityEffect(fireworks, (byte)17);
			fireworks.die();
		}
	}
	
	Field[] packet63Fields = new Field[9];
	@Override
	public void playParticleEffect(Location location, String name, float spreadHoriz, float spreadVert, float speed, int count, int radius, float yOffset) {
		Packet63WorldParticles packet = new Packet63WorldParticles();
		try {
			packet63Fields[0].set(packet, name);
			packet63Fields[1].setFloat(packet, (float)location.getX());
			packet63Fields[2].setFloat(packet, (float)location.getY() + yOffset);
			packet63Fields[3].setFloat(packet, (float)location.getZ());
			packet63Fields[4].setFloat(packet, spreadHoriz);
			packet63Fields[5].setFloat(packet, spreadVert);
			packet63Fields[6].setFloat(packet, spreadHoriz);
			packet63Fields[7].setFloat(packet, speed);
			packet63Fields[8].setInt(packet, count);
			
			int rSq = radius * radius;
			
			for (Player player : location.getWorld().getPlayers()) {
				if (player.getLocation().distanceSquared(location) <= rSq) {
					((CraftPlayer)player).getHandle().playerConnection.sendPacket(packet);
				} else {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setKiller(LivingEntity entity, Player killer) {
		((CraftLivingEntity)entity).getHandle().killer = ((CraftPlayer)killer).getHandle();
	}	@Override
	public DisguiseManager getDisguiseManager(MagicConfig config) {
		return new DisguiseManager_1_6_R2(config);
	}
	
	public class DisguiseManager_1_6_R2 implements Listener, DisguiseManager {
		private boolean hideArmor;
		
		private Set<DisguiseSpell> disguiseSpells = new HashSet<DisguiseSpell>();
		private Map<String, DisguiseSpell.Disguise> disguises = new HashMap<String, DisguiseSpell.Disguise>();
		private Set<Integer> disguisedEntityIds = new HashSet<Integer>();
		private Set<Integer> dragons = new HashSet<Integer>();

		private PacketListener packetListener = null;
		private Random random = new Random();
		
		public DisguiseManager_1_6_R2(MagicConfig config) {
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
			DisguiseSpell.Disguise disguise = disguises.remove(player.getName().toLowerCase());
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
			disguiseSpells.clear();
		}
		
		private void applyDisguise(Player player, DisguiseSpell.Disguise disguise) {
			sendDestroyEntityPackets(player);
			sendDisguisedSpawnPackets(player, disguise);
		}
		
		private void clearDisguise(Player player) {
			sendDestroyEntityPackets(player);
			if (player.isValid()) {
				sendPlayerSpawnPackets(player);
			}
		}

		private Entity getEntity(Player player, DisguiseSpell.Disguise disguise) {
			EntityType entityType = disguise.getEntityType();
			boolean flag = disguise.getFlag();
			int var = disguise.getVar1();
			Location location = player.getLocation();
			Entity entity = null;
			float yOffset = 0;
			World world = ((CraftWorld)location.getWorld()).getHandle();
			if (entityType == EntityType.PLAYER) {
				entity = new EntityHuman(world, disguise.getNameplateText()) {
					@Override
					public void sendMessage(ChatMessage arg0) {
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
				
			} else if (entityType.getTypeId() == 100 /*horse*/) {
				entity = new EntityHorse(world);
				((EntityAgeable)entity).setAge(flag ? -24000 : 0);
				((EntityHorse)entity).getDataWatcher().watch(19, Byte.valueOf((byte)disguise.getVar1()));
				((EntityHorse)entity).getDataWatcher().watch(20, Integer.valueOf(disguise.getVar2()));
				if (disguise.getVar3() > 0) {
					((EntityHorse)entity).getDataWatcher().watch(22, Integer.valueOf(disguise.getVar3()));
				}
				
			} else if (entityType == EntityType.ENDER_DRAGON) {
				entity = new EntityEnderDragon(world);
							
			} else if (entityType == EntityType.FALLING_BLOCK) {
				int id = disguise.getVar1();
				int data = disguise.getVar2();
				entity = new EntityFallingBlock(world, 0, 0, 0, id > 0 ? id : 1, data > 15 ? 0 : data);
				
			} else if (entityType == EntityType.DROPPED_ITEM) {
				int id = disguise.getVar1();
				int data = disguise.getVar2();
				entity = new EntityItem(world);
				((EntityItem)entity).setItemStack(new net.minecraft.server.v1_6_R2.ItemStack(id > 0 ? id : 1, 1, data));
				
			}
			
			if (entity != null) {
				
				String nameplateText = disguise.getNameplateText();
				if (entity instanceof EntityInsentient && nameplateText != null && !nameplateText.isEmpty()) {
					((EntityInsentient)entity).setCustomName(nameplateText);
					((EntityInsentient)entity).setCustomNameVisible(true);
				}
				
				if (player.hasPotionEffect(PotionEffectType.INVISIBILITY) && entity instanceof EntityLiving) {
					Collection<PotionEffect> effects = player.getActivePotionEffects();
					for (PotionEffect effect : effects) {
						if (effect.getType() == PotionEffectType.INVISIBILITY) {
							((LivingEntity)entity.getBukkitEntity()).addPotionEffect(effect);
						}
					}
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
				DisguiseSpell.Disguise disguise = getDisguise(p);
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
			DisguiseSpell.Disguise disguise = getDisguise(event.getPlayer());
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
				super(MagicSpells.plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL, 0x14, 0x28, 0x5, 0x20, 0x21, 0x22, 0x23);
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
						final DisguiseSpell.Disguise disguise = disguises.get(name.toLowerCase());
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
				} else if (event.getPacketID() == 0x20) {
					Packet32EntityLook packet = (Packet32EntityLook)event.getPacket().getHandle();
					if (packet.a < 0) {
						packet.a *= -1;
					} else if (dragons.contains(packet.a)) {
						Packet32EntityLook newpacket = new Packet32EntityLook();
						newpacket.a = -packet.a;
						int dir = packet.e + 128;
						if (dir > 127) dir -= 256;
						newpacket.e = (byte)dir;
						newpacket.f = 0;
						((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(newpacket);
						event.setCancelled(true);
					}
				} else if (event.getPacketID() == 0x21) {
					Packet33RelEntityMoveLook packet = (Packet33RelEntityMoveLook)event.getPacket().getHandle();
					if (packet.a < 0) {
						packet.a *= -1;
					} else if (dragons.contains(packet.a)) {
						Packet33RelEntityMoveLook newpacket = new Packet33RelEntityMoveLook();
						newpacket.a = -packet.a;
						newpacket.b = packet.b;
						newpacket.c = packet.c;
						newpacket.d = packet.d;
						int dir = packet.e + 128;
						if (dir > 127) dir -= 256;
						newpacket.e = (byte)dir;
						newpacket.f = 0;
						((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(newpacket);
						Packet28EntityVelocity packet28 = new Packet28EntityVelocity(packet.a, 0.15, 0, 0.15);
						((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(packet28);
						event.setCancelled(true);
					}
				} else if (event.getPacketID() == 0x22) {
					Packet34EntityTeleport packet = (Packet34EntityTeleport)event.getPacket().getHandle();
					if (packet.a < 0) {
						packet.a *= -1;
					} else if (dragons.contains(packet.a)) {
						Packet34EntityTeleport newpacket = new Packet34EntityTeleport();
						newpacket.a = -packet.a;
						newpacket.b = packet.b;
						newpacket.c = packet.c;
						newpacket.d = packet.d;
						int dir = packet.e + 128;
						if (dir > 127) dir -= 256;
						newpacket.e = (byte)dir;
						newpacket.f = 0;
						((CraftPlayer)event.getPlayer()).getHandle().playerConnection.sendPacket(newpacket);
						event.setCancelled(true);
					}
				} else if (event.getPacketID() == 0x23) {
					Packet35EntityHeadRotation packet = (Packet35EntityHeadRotation)event.getPacket().getHandle();
					if (dragons.contains(packet.a)) {
						event.setCancelled(true);
					}
				} else if (event.getPacketID() == 0x28) {
					Packet40EntityMetadata packet = (Packet40EntityMetadata)event.getPacket().getHandle();
					if (packet.a < 0) {
						packet.a *= -1;
					} else if (disguisedEntityIds.contains(packet.a) && !dragons.contains(packet.a)) {
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
		
		private void sendDisguisedSpawnPacket(Player viewer, Player disguised, DisguiseSpell.Disguise disguise, Entity entity) {
			if (entity == null) entity = getEntity(disguised, disguise);
			if (entity != null) {
				List<Packet> packets = getPacketsToSend(disguised, disguise, entity);
				if (packets != null && packets.size() > 0) {
					EntityPlayer ep = ((CraftPlayer)viewer).getHandle();
					for (Packet packet : packets) {
						ep.playerConnection.sendPacket(packet);
					}
				}
			}
		}
		
		private void sendDisguisedSpawnPackets(Player disguised, DisguiseSpell.Disguise disguise) {
			Entity entity = getEntity(disguised, disguise);
			if (entity != null) {
				List<Packet> packets = getPacketsToSend(disguised, disguise, entity);
				if (packets != null && packets.size() > 0) {
					final EntityTracker tracker = ((CraftWorld)disguised.getWorld()).getHandle().tracker;
					for (Packet packet : packets) {
						tracker.a(((CraftPlayer)disguised).getHandle(), packet);
					}
				}
			}
		}
		
		private List<Packet> getPacketsToSend(Player disguised, DisguiseSpell.Disguise disguise, Entity entity) {
			List<Packet> packets = new ArrayList<Packet>();
			if (entity instanceof EntityHuman) {
				Packet20NamedEntitySpawn packet20 = new Packet20NamedEntitySpawn((EntityHuman)entity);
				packet20.a = -disguised.getEntityId();
				packets.add(packet20);
				
				ItemStack inHand = disguised.getItemInHand();
				if (inHand != null && inHand.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 0, CraftItemStack.asNMSCopy(inHand));
					packets.add(packet5);
				}
				
				ItemStack helmet = disguised.getInventory().getHelmet();
				if (helmet != null && helmet.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 4, CraftItemStack.asNMSCopy(helmet));
					packets.add(packet5);
				}
				
				ItemStack chestplate = disguised.getInventory().getChestplate();
				if (chestplate != null && chestplate.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 3, CraftItemStack.asNMSCopy(chestplate));
					packets.add(packet5);
				}
				
				ItemStack leggings = disguised.getInventory().getLeggings();
				if (leggings != null && leggings.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 2, CraftItemStack.asNMSCopy(leggings));
					packets.add(packet5);
				}
				
				ItemStack boots = disguised.getInventory().getBoots();
				if (boots != null && boots.getType() != Material.AIR) {
					Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 1, CraftItemStack.asNMSCopy(boots));
					packets.add(packet5);
				}
			} else if (entity instanceof EntityLiving) {
				Packet24MobSpawn packet24 = new Packet24MobSpawn((EntityLiving)entity);
				packet24.a = disguised.getEntityId();
				if (dragons.contains(disguised.getEntityId())) {
					int dir = packet24.i + 128;
					if (dir > 127) dir -= 256;
					packet24.i = (byte)dir;
					packet24.j = 0;
					packet24.k = 1;
				}
				packets.add(packet24);
				if (dragons.contains(disguised.getEntityId())) {
					Packet28EntityVelocity packet28 = new Packet28EntityVelocity(disguised.getEntityId(), 0.15, 0, 0.15);
					packets.add(packet28);
				}
				
				if (disguise.getEntityType() == EntityType.ZOMBIE || disguise.getEntityType() == EntityType.SKELETON) {
					ItemStack inHand = disguised.getItemInHand();
					if (inHand != null && inHand.getType() != Material.AIR) {
						Packet5EntityEquipment packet5 = new Packet5EntityEquipment(disguised.getEntityId(), 0, CraftItemStack.asNMSCopy(inHand));
						packets.add(packet5);
					}
				}
			} else if (entity instanceof EntityFallingBlock) {
				Packet23VehicleSpawn packet23 = new Packet23VehicleSpawn(entity, 70, disguise.getVar1() | ((byte)disguise.getVar2()) << 16);
				packet23.a = disguised.getEntityId();
				packets.add(packet23);
			} else if (entity instanceof EntityItem) {
				Packet23VehicleSpawn packet23 = new Packet23VehicleSpawn(entity, 2, 1);
				packet23.a = disguised.getEntityId();
				packets.add(packet23);
				Packet40EntityMetadata packet40 = new Packet40EntityMetadata(-disguised.getEntityId(), entity.getDataWatcher(), true);
				packets.add(packet40);
			}
			return packets;
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

}
