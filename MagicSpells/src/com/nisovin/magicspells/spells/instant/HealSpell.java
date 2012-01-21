package com.nisovin.magicspells.spells.instant;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class HealSpell extends InstantSpell {
	
	private int healAmount;
	private boolean obeyLos;
	private String strNoTarget;
	private String strMaxHealth;
	private String strCastTarget;

	public HealSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		healAmount = config.getInt("spells." + spellName + ".heal-amount", 10);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "No target to heal.");
		strMaxHealth = config.getString("spells." + spellName + ".str-max-health", "%t is already at max health.");
		strCastTarget = config.getString("spells." + spellName + ".str-cast-target", "%a healed you.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Player target = getTargetedPlayer(player, range, obeyLos);
			if (target == null) {
				sendMessage(player, strNoTarget);
				fizzle(player);
			} else if (target.getHealth() == 20) {
				sendMessage(player, formatMessage(strMaxHealth, "%t", target.getName()));
			} else {				
				int health = target.getHealth();
				health += Math.round(healAmount*power);
				if (health > 20) health = 20;
				target.setHealth(health);
				
				sendMessage(player, formatMessage(strCastSelf, "%t", target.getDisplayName()));
				sendMessage(target, formatMessage(strCastTarget, "%a", player.getDisplayName()));
				sendMessageNear(player, formatMessage(strCastOthers, "%t", target.getDisplayName(), "%a", player.getDisplayName()));
				
				return PostCastAction.NO_MESSAGES;
			}
			
			return PostCastAction.ALREADY_HANDLED;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
