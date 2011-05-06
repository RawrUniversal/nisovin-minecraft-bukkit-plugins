package com.nisovin.MagicSystem;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

public abstract class EnhanceSpell extends Spell {
	
	protected ItemStack[] useCost;
	protected int useCostInterval;
	protected int numUses;
	protected int duration; // TODO
	
	private HashMap<String,Integer> useCounter;
	
	public EnhanceSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		List<String> costList = config.getStringList("spells." + spellName + ".use-cost", null);
		if (costList != null && costList.size() > 0) {
			useCost = new ItemStack [costList.size()];
			for (int i = 0; i < costList.size(); i++) {
				if (costList.get(i).contains(" ")) {
					String [] data = costList.get(i).split(" ");
					if (data[0].contains(":")) {
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
		
		if (useCost != null && useCostInterval > 0) {
			useCounter = new HashMap<String,Integer>();
		}
	}
	
	protected int addUse(Player player) {
		if (useCost != null && useCostInterval > 0) {
			Integer uses = useCounter.get(player.getName());
			if (uses == null) {
				uses = 1;
			} else {
				uses++;
			}
			
			if (numUses > 0 && uses > numUses) {
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
		if (useCost != null && useCostInterval > 0) {
			int uses = useCounter.get(player.getName());
			if (uses % useCostInterval == 0) {
				if (hasReagents(player, useCost)) {
					removeReagents(player, useCost);
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
		useCounter.remove(player.getName());
	}
	
}