package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;

public class GripSpell extends TargetedSpell implements TargetedEntitySpell, TargetedEntityFromLocationSpell {

	float locationOffset;
	
	public GripSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		locationOffset = getConfigFloat("location-offset", 0);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, power);
			if (target != null) {
				grip(player, target);
				sendMessages(player, target);
				return PostCastAction.NO_MESSAGES;
			} else {
				return noTarget(player);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void grip(Player player, LivingEntity target) {
		Location loc = player.getLocation().add(player.getLocation().getDirection().setY(0).normalize().multiply(locationOffset));
		if (!BlockUtils.isSafeToStand(loc)) {
			loc = player.getLocation();
		}
		playSpellEffects(player, target);
		target.teleport(loc);
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (validTargetList.canTarget(caster, target)) {
			grip(caster, target);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		return false;
	}

	@Override
	public boolean castAtEntityFromLocation(Player caster, Location from, LivingEntity target, float power) {
		return castAtEntityFromLocation(from, target, power);
	}

	@Override
	public boolean castAtEntityFromLocation(Location from, LivingEntity target, float power) {
		if (validTargetList.canTarget(target)) {
			Location loc = from.clone();
			loc.setX(loc.getBlockX() + .5);
			loc.setY(loc.getBlockY() + .5);
			loc.setZ(loc.getBlockZ() + .5);
			if (!BlockUtils.isSafeToStand(loc)) {
				loc.add(0, 1, 0);
				if (!BlockUtils.isSafeToStand(loc)) {
					return false;
				}
			}
			Location start = target.getLocation().clone();
			playSpellEffects(EffectPosition.TARGET, target);
			target.teleport(loc);
			playSpellEffectsTrail(start, loc);
			return true;
		} else {
			return false;
		}
	}

}
