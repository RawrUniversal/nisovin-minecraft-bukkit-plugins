package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class CleanseSpell extends TargetedSpell implements TargetedEntitySpell {

	boolean targetPlayers;
	boolean targetNonPlayers;
	boolean beneficial;
	
	List<PotionEffectType> potionEffectTypes;
	List<BuffSpell> buffSpells;
	boolean fire;
	
	private ValidTargetChecker checker;
	
	public CleanseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		targetPlayers = getConfigBoolean("target-players", true);
		targetNonPlayers = getConfigBoolean("target-non-players", false);
		beneficial = getConfigBoolean("beneficial", true);
		
		potionEffectTypes = new ArrayList<PotionEffectType>();
		buffSpells = new ArrayList<BuffSpell>();
		fire = false;
		List<String> toCleanse = getConfigStringList("remove", Arrays.asList(new String[] { "fire", "17", "19", "20" }));
		for (String s : toCleanse) {
			if (s.equalsIgnoreCase("fire")) {
				fire = true;
			} else if (s.matches("^[0-9]+$")) {
				int t = Integer.parseInt(s);
				PotionEffectType type = PotionEffectType.getById(t);
				if (type != null) {
					potionEffectTypes.add(type);
				}
			} else if (s.startsWith("buff:")) {
				Spell spell = MagicSpells.getSpellByInternalName(s.replace("buff:", ""));
				if (spell != null && spell instanceof BuffSpell) {
					buffSpells.add((BuffSpell)spell);
				}
			} else {
				PotionEffectType type = PotionEffectType.getByName(s.toUpperCase());
				if (type != null) {
					potionEffectTypes.add(type);
				}
			}
		}
		
		checker = new ValidTargetChecker() {
			@Override
			public boolean isValidTarget(LivingEntity entity) {
				if (fire && entity.getFireTicks() > 0) {
					return true;
				}
				for (PotionEffectType type : potionEffectTypes) {
					if (entity.hasPotionEffect(type)) {
						return true;
					}
				}
				if (entity instanceof Player) {
					for (BuffSpell spell : buffSpells) {
						if (spell.isActive((Player)entity)) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, checker);
			if (target == null) {
				return noTarget(player);
			}
			
			cleanse(target);
			
			sendMessages(player, target);
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void cleanse(LivingEntity target) {
		if (fire) {
			target.setFireTicks(0);
		}
		for (PotionEffectType type : potionEffectTypes) {
			target.addPotionEffect(new PotionEffect(type, 0, 0, true), true);
			target.removePotionEffect(type);
		}
		if (target instanceof Player) {
			for (BuffSpell spell : buffSpells) {
				spell.turnOff((Player)target);
			}
		}
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		cleanse(target);
		return true;
	}

	@Override
	public boolean castAtEntity(LivingEntity target, float power) {
		cleanse(target);
		return true;
	}
	
	@Override
	public boolean isBeneficial() {
		return beneficial;
	}
	
	@Override
	public ValidTargetChecker getValidTargetChecker() {
		return checker;
	}

}
