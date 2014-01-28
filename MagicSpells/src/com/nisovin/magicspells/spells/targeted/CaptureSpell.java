package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class CaptureSpell extends TargetedSpell implements TargetedEntitySpell {

	boolean powerAffectsQuantity;
	boolean addToInventory;
	
	public CaptureSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		powerAffectsQuantity = getConfigBoolean("power-affects-quantity", true);
		addToInventory = getConfigBoolean("add-to-inventory", false);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, getValidTargetChecker());
			if (target == null) {
				return noTarget(player);
			}
			boolean ok = castAtEntity(player, target, power);
			if (!ok) {
				return noTarget(player);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@Override
	public ValidTargetChecker getValidTargetChecker() {
		return new ValidTargetChecker() {
			@Override
			public boolean isValidTarget(LivingEntity entity) {
				return (!(entity instanceof Player) && entity.getType().isSpawnable());
			}
		};
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!target.getType().isSpawnable()) return false;
		ItemStack item = Util.getEggItemForEntityType(target.getType());
		if (item != null) {
			if (powerAffectsQuantity) {
				int q = Math.round(power);
				if (q > 1) {
					item.setAmount(q);
				}
			}
			target.remove();
			boolean added = false;
			if (addToInventory && caster != null) {
				added = Util.addToInventory(caster.getInventory(), item);
			}
			if (!added) {
				target.getWorld().dropItem(target.getLocation().add(0, 1, 0), item).setItemStack(item);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return castAtEntity(null, target, power);
	}

}
