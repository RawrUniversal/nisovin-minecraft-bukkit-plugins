package com.nisovin.magicspells.spells;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.passive.PassiveManager;
import com.nisovin.magicspells.spells.passive.PassiveTrigger;
import com.nisovin.magicspells.util.CastItem;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class PassiveSpell extends Spell {

	private static PassiveManager manager;
	
	private Random random = new Random();
	private boolean disabled = false;
	
	private List<String> triggers;
	private float chance;
	private boolean castWithoutTarget;
	private int delay;
	private boolean sendFailureMessages;
	
	private List<String> spellNames;
	private List<Spell> spells;
	
	public PassiveSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		if (manager == null) manager = new PassiveManager();
		
		triggers = getConfigStringList("triggers", null);
		chance = getConfigFloat("chance", 100F) / 100F;
		castWithoutTarget = getConfigBoolean("cast-without-target", false);
		delay = getConfigInt("delay", -1);
		sendFailureMessages = getConfigBoolean("send-failure-messages", false);
		
		spellNames = getConfigStringList("spells", null);
	}
	
	public static PassiveManager getManager() {
		return manager;
	}
	
	public List<Spell> getActivatedSpells() {
		return spells;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		
		// create spell list
		spells = new ArrayList<Spell>();
		if (spellNames != null) {
			for (String spellName : spellNames) {
				Spell spell = MagicSpells.getSpellByInternalName(spellName);
				if (spell != null) {
					spells.add(spell);
				}
			}
		}
		if (spells.size() == 0) {
			MagicSpells.error("Passive spell '" + name + "' has no spells defined!");
			return;
		}
		
		// get trigger
		int trigCount = 0;
		if (triggers != null) {
			for (String strigger : triggers) {
				String type = strigger;
				String var = null;
				if (strigger.contains(" ")) {
					String[] data = Util.splitParams(strigger, 2);
					type = data[0];
					var = data[1];
				}
				type = type.toLowerCase();
				
				PassiveTrigger trigger = PassiveTrigger.getByName(type);
				if (trigger != null) {
					manager.registerSpell(this, trigger, var);
					trigCount++;
				} else {
					MagicSpells.error("Invalid trigger '" + strigger + "' on passive spell '" + internalName + "'");
				}
			}
		}
		if (trigCount == 0) {
			MagicSpells.error("Passive spell '" + name + "' has no triggers defined!");
			return;
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		return PostCastAction.ALREADY_HANDLED;
	}
	
	public void activate(Player caster) {
		activate(caster, null, null);
	}
	
	public void activate(Player caster, LivingEntity target) {
		activate(caster, target, null);
	}
	
	public void activate(Player caster, Location location) {
		activate(caster, null, location);
	}
	
	public void activate(final Player caster, final LivingEntity target, final Location location) {
		if (delay < 0) {
			activateSpells(caster, target, location);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
				public void run() {
					activateSpells(caster, target, location);
				}
			}, delay);
		}
	}
	
	private void activateSpells(Player caster, LivingEntity target, Location location) {
		SpellCastState state = getCastState(caster);
		MagicSpells.debug(3, "Activating passive spell '" + name + "' for player " + caster.getName() + " (state: " + state + ")");
		if (!disabled && (chance >= .999 || random.nextFloat() <= chance) && state == SpellCastState.NORMAL) {
			disabled = true;
			SpellCastEvent event = new SpellCastEvent(this, caster, SpellCastState.NORMAL, 1.0F, null, this.cooldown, this.reagents.clone(), 0);
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled()) {
				if (event.haveReagentsChanged() && !hasReagents(caster, event.getReagents())) {
					return;
				}
				setCooldown(caster, event.getCooldown());
				float power = event.getPower();
				for (Spell spell : spells) {
					MagicSpells.debug(3, "    Casting spell effect '" + spell.getName() + "'");
					if (castWithoutTarget || !(spell instanceof TargetedSpell)) {
						spell.castSpell(caster, SpellCastState.NORMAL, power, null);
						playSpellEffects(EffectPosition.CASTER, caster);
					} else if (spell instanceof TargetedEntitySpell && target != null) {
						((TargetedEntitySpell)spell).castAtEntity(caster, target, power);
						playSpellEffects(caster, target);
					} else if (spell instanceof TargetedLocationSpell && (location != null || target != null)) {
						if (location != null) {
							((TargetedLocationSpell)spell).castAtLocation(caster, location, power);
							playSpellEffects(caster, location);
						} else if (target != null) {
							((TargetedLocationSpell)spell).castAtLocation(caster, target.getLocation(), power);
							playSpellEffects(caster, target.getLocation());
						}
					}
				}
				removeReagents(caster, event.getReagents());
				sendMessage(caster, strCastSelf);				
			} else {
				MagicSpells.debug(3, "   Passive spell canceled");
			}
			disabled = false;
		} else if (state != SpellCastState.NORMAL && sendFailureMessages) {
			if (state == SpellCastState.ON_COOLDOWN) {
				MagicSpells.sendMessage(caster, formatMessage(strOnCooldown, "%c", Math.round(getCooldown(caster))+""));
			} else if (state == SpellCastState.MISSING_REAGENTS) {
				MagicSpells.sendMessage(caster, strMissingReagents);
				if (MagicSpells.showStrCostOnMissingReagents() && strCost != null && !strCost.isEmpty()) {
					MagicSpells.sendMessage(caster, "    (" + strCost + ")");
				}
			}
		}
	}
	
	@Override
	public boolean canBind(CastItem item) {
		return false;
	}

	@Override
	public boolean canCastWithItem() {
		return false;
	}

	@Override
	public boolean canCastByCommand() {
		return false;
	}

}
