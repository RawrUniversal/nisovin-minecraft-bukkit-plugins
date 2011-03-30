package com.nisovin.MagicSystem;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public abstract class Spell {

	protected String name;
	protected String description;
	protected int [][] cost;
	protected int cooldown;
	protected int broadcastRange;
	protected String strCost;
	protected String strCastSelf;
	protected String strCastOthers;
	
	private HashMap<String, Long> lastCast;

	public Spell(Configuration config, String spellName) {
		this.name = config.getString("spells." + spellName + ".name", spellName);
		this.description = config.getString("spells." + spellName + ".description", "");
		List<String> costList = config.getStringList("spells." + spellName + ".cost", null);
		if (costList != null && costList.size() > 0) {
			cost = new int [costList.size()][2];
			for (int i = 0; i < costList.size(); i++) {
				String [] data = costList.get(i).split(" ");
				cost[i][0] = Integer.parseInt(data[0]);
				cost[i][1] = Integer.parseInt(data[1]);
			}
		} else {
			cost = null;
		}
		this.cooldown = config.getInt("spells." + spellName + ".cooldown", 0);
		this.broadcastRange = config.getInt("spells." + spellName + ".broadcast-range", MagicSystem.broadcastRange);
		this.strCost = config.getString("spells." + spellName + ".str-cost", null);
		this.strCastSelf = config.getString("spells." + spellName + ".str-cast-self", null);
		this.strCastOthers = config.getString("spells." + spellName + ".str-cast-others", null);
		
		if (cooldown > 0) {
			lastCast = new HashMap<String, Long>();
		}
	}

	public void cast(Player player) {
		SpellCastState state;
		if (onCooldown(player)) {
			state = SpellCastState.ON_COOLDOWN;
		} else if (!hasReagents(player)) {
			state = SpellCastState.MISSING_REAGENTS;
		} else {
			state = SpellCastState.NORMAL;
		}
		
		boolean handleIt = castSpell(player, state);
		if (handleIt) {
			if (state == SpellCastState.NORMAL) {
				setCooldown(player);
				removeReagents(player);
			} else if (state == SpellCastState.ON_COOLDOWN) {
				player.sendMessage(MagicSystem.strOnCooldown);
			} else if (state == SpellCastState.MISSING_REAGENTS) {
				player.sendMessage(MagicSystem.strMissingReagents);
			}
		}
	}
	
	public String getCostStr() {
		if (strCost == null || strCost.equals("")) {
			return null;
		} else {
			return strCost;
		}
	}
	
	protected boolean onCooldown(Player player) {
		if (cooldown == 0) {
			return false;
		}
		
		Long casted = lastCast.get(player.getName());
		if (casted != null) {
			if (casted + (cooldown*1000) > System.currentTimeMillis()) {
				return true;
			}
		}
		return false;
	}
	
	protected void setCooldown(Player player) {
		if (cooldown > 0) {
			lastCast.put(player.getName(), System.currentTimeMillis());
		}
	}
	
	protected boolean hasReagents(Player player) {
		return true;
	}
	
	protected void removeReagents(Player player) {
		removeReagents(player, cost);
	}
	
	protected void removeReagents(Player player, int [][] reagents) {
	}
	
	protected void sendMessage(Player player, String message) {
		if (message != null && message != "") {
			player.sendMessage(message);
		}
	}
	
	protected abstract boolean castSpell(Player player, SpellCastState state);

	protected enum SpellCastState {
		NORMAL,
		ON_COOLDOWN,
		MISSING_REAGENTS
	}

}
