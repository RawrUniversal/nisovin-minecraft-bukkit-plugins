package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ChainSpell extends TargetedSpell implements TargetedEntitySpell {

	String spellNameToCast;
	TargetedSpell spellToCast;
	ValidTargetChecker checker;
	int bounces;
	int bounceRange;
	int interval;
	boolean beneficial;
	boolean targetPlayers;
	boolean targetNonPlayers;
	
	public ChainSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		spellNameToCast = getConfigString("spell", "heal");
		bounces = getConfigInt("bounces", 3);
		bounceRange = getConfigInt("bounce-range", 8);
		interval = getConfigInt("interval", 10);
		beneficial = getConfigBoolean("beneficial", true);
		targetPlayers = getConfigBoolean("target-players", true);
		targetNonPlayers = getConfigBoolean("target-non-players", false);
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		Spell spell = MagicSpells.getSpellByInternalName(spellNameToCast);
		if (spell == null || !(spell instanceof TargetedSpell)) {
			MagicSpells.error("Invalid spell defined for ChainSpell '" + this.name + "'");
		} else {
			spellToCast = (TargetedSpell)spell;
			checker = spellToCast.getValidTargetChecker();
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, checker);
			if (target == null) {
				return noTarget(player);
			}
			chain(player, target, power);
			sendMessages(player, target);
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void chain(Player player, LivingEntity target, float power) {
		List<LivingEntity> targets = new ArrayList<LivingEntity>();
		targets.add(target);
		
		// get targets
		LivingEntity current = target;
		int attempts = 0;
		while (targets.size() < bounces && attempts++ < bounces * 2) {
			List<Entity> entities = current.getNearbyEntities(bounceRange, bounceRange, bounceRange);
			for (Entity e : entities) {
				if (!(e instanceof LivingEntity)) {
					continue;
				}
				if (targets.contains(e)) {
					continue;
				}
				if (e instanceof Player) {
					if (!targetPlayers) {
						continue;
					}
				} else if (!targetNonPlayers) {
					continue;
				}
				if (checker != null && !checker.isValidTarget((LivingEntity)e)) {
					continue;
				}
				SpellTargetEvent event = new SpellTargetEvent(this, player, (LivingEntity)e);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					continue;
				}
				
				targets.add((LivingEntity)e);
				current = (LivingEntity)e;
				break;
			}
		}
		
		// cast spell at targets
		playSpellEffects(EffectPosition.CASTER, player);
		if (interval <= 0) {
			for (int i = 0; i < targets.size(); i++) {
				castSpellAt(player, i == 0 ? player.getLocation() : targets.get(i-1).getLocation(), targets.get(i), power);
				if (i > 0) {
					playSpellEffectsTrail(targets.get(i-1).getLocation(), targets.get(i).getLocation());
				} else if (i == 0) {
					playSpellEffectsTrail(player.getLocation(), targets.get(i).getLocation());
				}
				playSpellEffects(EffectPosition.TARGET, targets.get(i));
			}
		} else {
			new ChainBouncer(player, targets, power);
		}
	}
	
	private boolean castSpellAt(Player caster, Location from, LivingEntity target, float power) {
		if (spellToCast instanceof TargetedEntityFromLocationSpell) {
			return ((TargetedEntityFromLocationSpell)spellToCast).castAtEntityFromLocation(caster, from, target, power);
		} else if (spellToCast instanceof TargetedEntitySpell) {
			return ((TargetedEntitySpell)spellToCast).castAtEntity(caster, target, power);
		} else if (spellToCast instanceof TargetedLocationSpell) {
			return ((TargetedLocationSpell)spellToCast).castAtLocation(caster, target.getLocation(), power);
		} else {
			return true;
		}
	}

	@Override
	public boolean castAtEntity(Player caster, LivingEntity target, float power) {
		chain(caster, target, power);
		return true;
	}
	
	@Override
	public boolean isBeneficial() {
		return beneficial;
	}

	class ChainBouncer implements Runnable {
		Player caster;
		List<LivingEntity> targets;
		float power;
		int current = 0;
		int taskId;
		
		public ChainBouncer(Player caster, List<LivingEntity> targets, float power) {
			this.caster = caster;
			this.targets = targets;
			this.power = power;
			taskId = MagicSpells.scheduleRepeatingTask(this, 0, interval);
		}
		
		public void run() {
			castSpellAt(caster, current == 0 ? caster.getLocation() : targets.get(current-1).getLocation(), targets.get(current), power);
			if (current > 0) {
				playSpellEffectsTrail(targets.get(current-1).getLocation().add(0, .5, 0), targets.get(current).getLocation().add(0, .5, 0));
			} else if (current == 0) {
				playSpellEffectsTrail(caster.getLocation().add(0, .5, 0), targets.get(current).getLocation().add(0, .5, 0));
			}
			playSpellEffects(EffectPosition.TARGET, targets.get(current));
			current++;
			if (current >= targets.size()) {
				MagicSpells.cancelTask(taskId);
			}
		}
	}
	
}
