package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class PotionEffectSpell extends TargetedSpell implements TargetedEntitySpell {
	
	private int type;
	private int duration;
	private int amplifier;
	private boolean ambient;
	private boolean targeted;
	private boolean beneficial;
	
	public PotionEffectSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		type = getConfigInt("type", 0);
		duration = getConfigInt("duration", 0);
		amplifier = getConfigInt("strength", 0);
		ambient = getConfigBoolean("ambient", false);
		targeted = getConfigBoolean("targeted", false);
		beneficial = getConfigBoolean("beneficial", false);
	}
	
	public int getType() {
		return type;
	}
	
	public int getDuration() {
		return duration;
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target;
			if (targeted) {
				target = getTargetedEntity(player);
			} else {
				target = player;
			}
			if (target == null) {
				// fail no target
				return noTarget(player);
			}
			
			target.addPotionEffect(new PotionEffect(PotionEffectType.getById(type), Math.round(duration*power), amplifier, ambient), true);
			if (targeted) {
				playSpellEffects(player, target);
			} else {
				playSpellEffects(EffectPosition.CASTER, player);
			}
			sendMessages(player, target);
			return PostCastAction.NO_MESSAGES;
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		if (!validTargetList.canTarget(caster, target)) {
			return false;
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.getById(type), Math.round(duration*power), amplifier, ambient);
			if (targeted) {
				target.addPotionEffect(effect, true);
				playSpellEffects(caster, target);
			} else {
				caster.addPotionEffect(effect, true);
				playSpellEffects(EffectPosition.CASTER, caster);
			}
			return true;
		}
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		if (!validTargetList.canTarget(target)) {
			return false;
		} else {
			PotionEffect effect = new PotionEffect(PotionEffectType.getById(type), Math.round(duration*power), amplifier, ambient);
			target.addPotionEffect(effect);
			playSpellEffects(EffectPosition.TARGET, target);
			return true;
		}			
	}
	
	@Override
	public boolean isBeneficial() {
		return beneficial;
	}

}
