package com.nisovin.MagicSpells;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

public abstract class BuffSpell extends Spell {
	
	protected ItemStack[] useCost;
	protected int healthCost = 0;
	protected int manaCost = 0;
	protected int useCostInterval;
	protected int numUses;
	protected int duration;
	protected String strFade;
	private boolean castWithItem;
	private boolean castByCommand;
	
	private HashMap<String,Integer> useCounter;
	private HashMap<String,Long> durationStartTime;
	
	public BuffSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		List<String> costList = config.getStringList("spells." + spellName + ".use-cost", null);
		if (costList != null && costList.size() > 0) {
			useCost = new ItemStack [costList.size()];
			for (int i = 0; i < costList.size(); i++) {
				if (costList.get(i).contains(" ")) {
					String [] data = costList.get(i).split(" ");
					if (data[0].equalsIgnoreCase("health")) {
						healthCost = Integer.parseInt(data[1]);
					} else if (data[0].equalsIgnoreCase("mana")) {
						manaCost = Integer.parseInt(data[1]);
					} else if (data[0].contains(":")) {
						String [] subdata = data[0].split(":");
						useCost[i] = new ItemStack(Integer.parseInt(subdata[0]), Integer.parseInt(data[1]), Short.parseShort(subdata[1]));
					} else {
						useCost[i] = new ItemStack(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
					}
				} else {
					useCost[i] = new ItemStack(Integer.parseInt(costList.get(i)));
				}
			}
		} else {
			useCost = null;
		}
		useCostInterval = config.getInt("spells." + spellName + ".use-cost-interval", 0);
		numUses = config.getInt("spells." + spellName + ".num-uses", 0);
		duration = config.getInt("spells." + spellName + ".duration", 0);
		
		strFade = config.getString("spells." + spellName + ".str-fade", "");
		
		if (numUses > 0 || (useCost != null && useCostInterval > 0)) {
			useCounter = new HashMap<String,Integer>();
		}
		if (duration > 0) {
			durationStartTime = new HashMap<String,Long>();
		}
		
		castWithItem = config.getBoolean("spells." + spellName + ".can-cast-with-item", true);
		castByCommand = config.getBoolean("spells." + spellName + ".can-cast-by-command", true);
	}
	
	public boolean canCastWithItem() {
		return castWithItem;
	}
	
	public boolean canCastByCommand() {
		return castByCommand;
	}
	
	protected void startSpellDuration(Player player) {
		if (duration > 0 && durationStartTime != null) {
			durationStartTime.put(player.getName(), System.currentTimeMillis());
		}
	}
	
	protected boolean isExpired(Player player) {
		if (duration <= 0 || durationStartTime == null) {
			return false;
		} else {
			Long startTime = durationStartTime.get(player.getName());
			if (startTime == null) {
				return false;
			} else if (startTime + duration*1000 > System.currentTimeMillis()) {
				return false;
			} else {
				return true;
			}			
		}
	}
	
	protected int addUse(Player player) {
		if (numUses > 0 || (useCost != null && useCostInterval > 0)) {
			Integer uses = useCounter.get(player.getName());
			if (uses == null) {
				uses = 1;
			} else {
				uses++;
			}
			
			if (numUses > 0 && uses >= numUses) {
				turnOff(player);
			} else {
				useCounter.put(player.getName(), uses);
			}
			return uses;
		} else {
			return 0;
		}
	}
	
	protected boolean chargeUseCost(Player player) {
		if (useCost != null && useCostInterval > 0 && useCounter != null && useCounter.containsKey(player.getName())) {
			int uses = useCounter.get(player.getName());
			if (uses % useCostInterval == 0) {
				if (hasReagents(player, useCost, healthCost, manaCost)) {
					removeReagents(player, useCost, healthCost, manaCost);
					return true;
				} else {
					turnOff(player);
					return false;
				}
			}
		}
		return true;
	}
	
	protected void turnOff(Player player) {
		if (useCounter != null) useCounter.remove(player.getName());
		if (durationStartTime != null) durationStartTime.remove(player.getName());
	}
	
	protected abstract void turnOff();
	
}