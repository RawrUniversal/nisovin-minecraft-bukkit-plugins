package com.nisovin.magicspells.spells.targeted;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.util.MagicConfig;

public class SpawnMonsterSpell extends TargetedSpell implements TargetedLocationSpell {

	private String location;
	private EntityType entityType;
	private boolean allowSpawnInMidair;
	private boolean baby;
	private boolean tamed;
	
	private ItemStack holding;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	private float holdingDropChance;
	private float helmetDropChance;
	private float chestplateDropChance;
	private float leggingsDropChance;
	private float bootsDropChance;
	private int duration;
	private String nameplateText;
	private boolean useCasterName;
	
	private Random random = new Random();
	
	public SpawnMonsterSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		location = getConfigString("location", "target");
		entityType = EntityType.fromName(getConfigString("entity-type", "wolf"));
		allowSpawnInMidair = getConfigBoolean("allow-spawn-in-midair", false);
		baby = getConfigBoolean("baby", false);
		tamed = getConfigBoolean("tamed", false);
		holding = Util.getItemStackFromString(getConfigString("holding", "0"));
		if (holding != null && holding.getTypeId() > 0) {
			holding.setAmount(1);
		}
		helmet = Util.getItemStackFromString(getConfigString("helmet", "0"));
		if (helmet != null && helmet.getTypeId() > 0) {
			helmet.setAmount(1);
		}
		chestplate = Util.getItemStackFromString(getConfigString("chestplate", "0"));
		if (chestplate != null && chestplate.getTypeId() > 0) {
			chestplate.setAmount(1);
		}
		leggings = Util.getItemStackFromString(getConfigString("leggings", "0"));
		if (leggings != null && leggings.getTypeId() > 0) {
			leggings.setAmount(1);
		}
		boots = Util.getItemStackFromString(getConfigString("boots", "0"));
		if (boots != null && boots.getTypeId() > 0) {
			boots.setAmount(1);
		}
		holdingDropChance = getConfigFloat("holding-drop-chance", 0) / 100F;
		helmetDropChance = getConfigFloat("helmet-drop-chance", 0) / 100F;
		chestplateDropChance = getConfigFloat("chestplate-drop-chance", 0) / 100F;
		leggingsDropChance = getConfigFloat("leggings-drop-chance", 0) / 100F;
		bootsDropChance = getConfigFloat("boots-drop-chance", 0) / 100F;
		duration = getConfigInt("duration", 0);
		nameplateText = getConfigString("nameplate-text", "");
		useCasterName = getConfigBoolean("use-caster-name", false);
		
		if (entityType == null || !entityType.isAlive()) {
			MagicSpells.error("SpawnMonster spell '" + spellName + "' has an invalid entity-type!");
		}
		
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Location loc = null;
			
			if (location.equalsIgnoreCase("target")) {
				Block block = player.getTargetBlock(MagicSpells.getTransparentBlocks(), range);
				if (block != null && block.getType() != Material.AIR) { 
					if (BlockUtils.isPathable(block)) {
						loc = block.getLocation();
					} else if (BlockUtils.isPathable(block.getRelative(BlockFace.UP))) {
						loc = block.getLocation().add(0, 1, 0);
					}
				}
			} else if (location.equalsIgnoreCase("caster")) {
				loc = player.getLocation();
			} else if (location.equalsIgnoreCase("random")) {				
				loc = getRandomLocationFrom(player.getLocation());				
			}
			
			if (loc == null) {
				return noTarget(player);
			}
			
			spawnMob(player, player.getLocation(), loc);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	private Location getRandomLocationFrom(Location location) {
		World world = location.getWorld();
		int attempts = 0;
		int x, y, z;
		Block block, block2;
		while (attempts < 10) {
			x = location.getBlockX() + random.nextInt(range * 2) - range;
			y = location.getBlockY() + 2;
			z = location.getBlockZ() + random.nextInt(range * 2) - range;	
			
			block = world.getBlockAt(x, y, z);
			if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
				return block.getLocation();
			} else if (BlockUtils.isPathable(block)) {
				if (allowSpawnInMidair) {
					return block.getLocation();
				}
				int c = 0;
				while (c < 5) {
					block2 = block.getRelative(BlockFace.DOWN);
					if (BlockUtils.isPathable(block2)) {
						block = block2;
					} else {
						return block.getLocation();
					}
					c++;
				}
			}
			
			attempts++;
		}
		return null;
	}
	
	private void spawnMob(Player player, Location source, Location loc) {
		if (entityType != null) {
			// spawn it
			loc.setYaw((float) (Math.random() * 360));
			final Entity entity = loc.getWorld().spawnEntity(loc.add(.5, .1, .5), entityType);
			// set as baby
			if (baby) {
				if (entity instanceof Ageable) {
					((Ageable)entity).setBaby();
				} else if (entity instanceof Zombie) {
					((Zombie)entity).setBaby(true);
				}
			}
			// set as tamed
			if (tamed && entity instanceof Tameable && player != null) {
				((Tameable)entity).setTamed(true);
				((Tameable)entity).setOwner(player);
			}
			// set held item
			if (holding != null && holding.getTypeId() > 0) {
				if (entity instanceof Enderman) {
					((Enderman)entity).setCarriedMaterial(new MaterialData(holding.getTypeId(), (byte)holding.getDurability()));
				} else if (entity instanceof Skeleton || entity instanceof Zombie) {
					EntityEquipment equip = ((LivingEntity)entity).getEquipment();
					equip.setItemInHand(holding.clone());
					equip.setItemInHandDropChance(holdingDropChance);
				}
			}
			// set armor
			EntityEquipment equip = ((LivingEntity)entity).getEquipment();
			equip.setHelmet(helmet);
			equip.setChestplate(chestplate);
			equip.setLeggings(leggings);
			equip.setBoots(boots);
			equip.setHelmetDropChance(helmetDropChance);
			equip.setChestplateDropChance(chestplateDropChance);
			equip.setLeggingsDropChance(leggingsDropChance);
			equip.setBootsDropChance(bootsDropChance);
			// set nameplate text
			if (entity instanceof LivingEntity) {
				if (useCasterName && player != null) {
					((LivingEntity)entity).setCustomName(player.getDisplayName());
					((LivingEntity)entity).setCustomNameVisible(true);
				} else if (nameplateText != null && !nameplateText.isEmpty()) {
					((LivingEntity)entity).setCustomName(nameplateText);
					((LivingEntity)entity).setCustomNameVisible(true);
				}
			}
			// play effects
			if (player != null) {
				playSpellEffects(player, entity);
			} else {
				playSpellEffects(source, entity);
			}
			// schedule removal
			if (duration > 0) {
				MagicSpells.scheduleDelayedTask(new Runnable() {
					public void run() {
						entity.remove();
					}
				}, duration);
			}
		}
	}
	
	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		if (location.equalsIgnoreCase("target")) {
			spawnMob(caster, caster.getLocation(), target);
		} else if (location.equalsIgnoreCase("caster")) {
			spawnMob(caster, caster.getLocation(), caster.getLocation());
		} else if (location.equalsIgnoreCase("random")) {
			Location loc = getRandomLocationFrom(target);
			if (loc != null) {
				spawnMob(caster, caster.getLocation(), loc);
			}
		}
		return true;
	}
	
	@Override
	public boolean castAtLocation(Location target, float power) {
		if (location.equalsIgnoreCase("target")) {
			spawnMob(null, target, target);
		} else if (location.equalsIgnoreCase("caster")) {
			spawnMob(null, target, target);
		} else if (location.equalsIgnoreCase("random")) {
			Location loc = getRandomLocationFrom(target);
			if (loc != null) {
				spawnMob(null, target, loc);
			}
		}
		return true;
	}
	
}
