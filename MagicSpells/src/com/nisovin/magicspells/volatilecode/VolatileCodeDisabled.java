package com.nisovin.magicspells.volatilecode;

import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VolatileCodeDisabled implements VolatileCodeHandle {

	@Override
	public void addPotionGraphicalEffect(LivingEntity entity, int color, int duration) {
	}

	@Override
	public void entityPathTo(LivingEntity entity, LivingEntity target) {
	}

	@Override
	public void queueChunksForUpdate(Player player, Set<Chunk> chunks) {
	}

	@Override
	public void sendFakeSlotUpdate(Player player, int slot, ItemStack item) {
	}
	
	@Override
	public void toggleLeverOrButton(Block block) {
		if (block.getType() == Material.STONE_BUTTON) {
			block.setData((byte) (block.getData() ^ 0x1));
		} else {
			byte data = block.getData();
			byte var1 = (byte) (data & 7);
			byte var2 = (byte) (8 - (data & 8));
			block.setData((byte) (var1 + var2));
		}
	}

	@Override
	public void pressPressurePlate(Block block) {
		block.setData((byte) (block.getData() ^ 0x1));
	}

	@Override
	public void removeMobEffect(LivingEntity entity, PotionEffectType type) {
		entity.addPotionEffect(new PotionEffect(type, 1, 0), true);
		entity.removePotionEffect(type);
	}

	@Override
	public boolean simulateTnt(Location target, float explosionSize, boolean fire) {
		return false;
	}

	@Override
	public boolean createExplosionByPlayer(Player player, Location location, float size, boolean fire, boolean breakBlocks) {
		return location.getWorld().createExplosion(location, size, fire);
	}

	@Override
	public void setExperienceBar(Player player, int level, float percent) {
	}

	@Override
	public Fireball shootSmallFireball(Player player) {
		return player.launchProjectile(SmallFireball.class);
	}

	@Override
	public void setTarget(LivingEntity entity, LivingEntity target) {
		if (entity instanceof Creature) {
			((Creature)entity).setTarget(target);
		}
	}

	@Override
	public void playSound(Location location, String sound, float volume, float pitch) {
	}

	@Override
	public void playSound(Player player, String sound, float volume, float pitch) {
	}

	@Override
	public boolean itemStackTagsEqual(ItemStack item1, ItemStack item2) {
		return true;
	}

	@Override
	public ItemStack addFakeEnchantment(ItemStack item) {
		item.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
		return item;
	}

	@Override
	public void setFallingBlockHurtEntities(FallingBlock block, float damage, int max) {
	}

	@Override
	public void addPotionEffect(LivingEntity entity, PotionEffect effect, boolean ambient) {
		entity.addPotionEffect(effect, true);
	}
	
	@Override
	public void playEntityAnimation(Location location, EntityType entityType, int animationId, boolean instant) {
		if (entityType == EntityType.OCELOT && animationId == 7) {
			Ocelot entity = (Ocelot)location.getWorld().spawnEntity(location, entityType);
			entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0));
			entity.playEffect(EntityEffect.WOLF_HEARTS);
			entity.remove();
		}
	}
}
