package com.nisovin.MagicSpells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class LightningSpell extends InstantSpell {

	private static final String SPELL_NAME = "lightning";
	
	private boolean requireEntityTarget;
	private int precision;
	private boolean obeyLos;
	private boolean targetPlayers;
	private boolean checkPlugins;
	private boolean noDamage;
	private String strCastFail;
	private String strNoTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new LightningSpell(config, spellName));
		}
	}
	
	public LightningSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		requireEntityTarget = config.getBoolean("spells." + spellName + ".require-entity-target", false);
		precision = config.getInt("spells." + spellName + ".precision", 20);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		checkPlugins = config.getBoolean("spells." + spellName + ".check-plugins", true);
		noDamage = config.getBoolean("spells." + spellName + ".no-damage", false);
		strCastFail = config.getString("spells." + spellName + ".str-cast-fail", "");
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "Unable to find target.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = null;
			if (requireEntityTarget) {
				LivingEntity e = getTargetedEntity(player, range>0?range:100, precision, targetPlayers, obeyLos);
				if (e != null && e instanceof Player && checkPlugins) {
					EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(player, e, DamageCause.ENTITY_ATTACK, 1);
					Bukkit.getServer().getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						e = null;
					}					
				}
				if (e != null) {
					target = e.getLocation().getBlock();
				} else {
					sendMessage(player, strNoTarget);
					return true;
				}
			} else {
				target = player.getTargetBlock(null, range>0?range:500);
				if (target.getWorld().getHighestBlockYAt(target.getLocation()) != target.getY()+1) {
					target = null;
				}
			}
			if (target != null) {
				if (noDamage) {
					target.getWorld().strikeLightningEffect(target.getLocation());
				} else {				
					target.getWorld().strikeLightning(target.getLocation());
				}
			} else {
				sendMessage(player, strCastFail);
				return true;
			}
		}
		return false;
	}
}
