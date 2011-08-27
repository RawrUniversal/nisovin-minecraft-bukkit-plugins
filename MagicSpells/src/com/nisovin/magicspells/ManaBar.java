package com.nisovin.MagicSpells;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ManaBar {
	private int mana;
	private int maxMana;
	
	public ManaBar(int maxMana) {
		this.maxMana = maxMana;
		this.mana = maxMana;
	}
	
	public boolean has(int amount) {
		return (mana >= amount);
	}
	
	public boolean remove(int amount) {
		if (amount > mana) {
			return false;
		} else {
			mana -= amount;
			return true;
		}
	}
	
	public boolean add(int amount) {
		if (mana == maxMana) {
			return false;
		}
		mana += amount;
		if (mana > maxMana) {
			mana = maxMana;
		}
		return true;
	}
	
	public void show(Player player) {
		int segments = (int)(((double)mana/(double)maxMana) * MagicSpells.manaBarSize);
		String text = MagicSpells.textColor + MagicSpells.manaBarPrefix + " {" + MagicSpells.manaBarColorFull;
		int i = 0;
		for (; i < segments; i++) {
			text += "=";
		}
		text += MagicSpells.manaBarColorEmpty;
		for (; i < MagicSpells.manaBarSize; i++) {
			text += "=";
		}
		text += MagicSpells.textColor + "} [" + mana + "/" + maxMana + "]";
		player.sendMessage(text);
	}
	
	public void showOnTool(Player player) {
		ItemStack item = player.getInventory().getItem(MagicSpells.manaBarToolSlot);
		Material type = item.getType();
		if (type == Material.WOOD_AXE || type == Material.WOOD_HOE || type == Material.WOOD_PICKAXE || type == Material.WOOD_SPADE || type == Material.WOOD_SWORD) {
			int dur = 60 - (int)(((double)mana/(double)maxMana) * 60);
			if (dur == 60) {
				dur = 59;
			} else if (dur == 0) {
				dur = 1;
			}
			item.setDurability((short)dur);
			player.getInventory().setItem(MagicSpells.manaBarToolSlot, item);
		}
	}
	
	public boolean regenerate(int percent) {
		if (mana < maxMana) {
			mana += (maxMana*(percent/100.0));
			if (mana > maxMana) {
				mana = maxMana;
			}
			return true;
		} else {
			return false;
		}
	}
}