package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;

public class GripSpell extends TargetedSpell implements TargetedEntitySpell {

	float locationOffset;
	
	public GripSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		locationOffset = getConfigFloat("location-offset", 0);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player);
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

}
