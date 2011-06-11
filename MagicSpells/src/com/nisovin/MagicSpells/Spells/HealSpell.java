package com.nisovin.MagicSpells.Spells;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.healthbars.HealthBars;

public class HealSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "heal";
	
	private int healAmount;
	private int precision;
	private boolean obeyLos;
	private String strNoTarget;
	private String strMaxHealth;
	private String strCastTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new HealSpell(config, spellName));
		}
	}

	public HealSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		healAmount = config.getInt("spells." + spellName + ".heal-amount", 10);
		precision = config.getInt("spells." + spellName + ".precision", 15);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "No target to heal.");
		strMaxHealth = config.getString("spells." + spellName + ".str-max-health", "%t is already at max health.");
		strCastTarget = config.getString("spells." + spellName + ".str-cast-target", "%a healed you.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Player target = getTargetedPlayer(player, range, precision, obeyLos);
			if (target == null) {
				sendMessage(player, strNoTarget);
			} else if (target.getHealth() == 20) {
				sendMessage(player, formatMessage(strMaxHealth, "%t", target.getName()));
			} else {
				setCooldown(player);
				removeReagents(player);
				
				int health = target.getHealth();
				health += healAmount;
				if (health > 20) health = 20;
				target.setHealth(health);
				
				sendMessage(player, formatMessage(strCastSelf, "%t", target.getName()));
				sendMessage(target, formatMessage(strCastTarget, "%a", player.getName()));
				sendMessageNear(player, formatMessage(strCastOthers, "%t", target.getName(), "%a", player.getName()));
				
				if (MagicSpells.useHealthBars) {
					HealthBars.queueHealthUpdate(target);
				}
			}
			
			return true;
		}
		return false;
	}

}
