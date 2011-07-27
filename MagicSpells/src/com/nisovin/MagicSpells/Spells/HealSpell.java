package com.nisovin.MagicSpells.Spells;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;

public class HealSpell extends InstantSpell {
	
	private int healAmount;
	private boolean obeyLos;
	private String strNoTarget;
	private String strMaxHealth;
	private String strCastTarget;

	public HealSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		healAmount = config.getInt("spells." + spellName + ".heal-amount", 10);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "No target to heal.");
		strMaxHealth = config.getString("spells." + spellName + ".str-max-health", "%t is already at max health.");
		strCastTarget = config.getString("spells." + spellName + ".str-cast-target", "%a healed you.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Player target = getTargetedPlayer(player, range, obeyLos);
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
			}
			
			return PostCastAction.ALREADY_HANDLED;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
