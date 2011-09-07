package com.nisovin.oldgods.godhandlers;

import java.util.HashSet;

import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.util.SpellReagents;

public class WisdomHandler {

	public static void onSpellCast(SpellCastEvent event) {
		SpellReagents reagents = event.getReagents();
		reagents.setHealth(reagents.getHealth() / 2);
		reagents.setMana(reagents.getMana() / 2);
		HashSet<ItemStack> items = reagents.getItems();
		for (ItemStack item : items) {
			if (item.getAmount() > 1) {
				item.setAmount(item.getAmount() / 2);
			}
		}
	}
	
}
