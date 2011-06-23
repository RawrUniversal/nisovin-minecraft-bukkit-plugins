package com.nisovin.MagicSpells;

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
	
	public void show(Player player) {
		int segments = (mana/maxMana) * MagicSpells.manaBarSize;
		String text = MagicSpells.manaBarPrefix + " [" + mana + "/" + maxMana + "] {";
		int i = 0;
		for (i; i <= segements; i++) {
			text += "=";
		}
		for (i; i <= MagicSpells.manaBarSize; i++) {
			text += " ";
		}
		text += "}";
		player.sendMessage(text);
	}
	
	public void regenerate(int percent) {
		if (mana < maxMana) {
			mana += (maxMana*percent);
			if (mana > maxMana) {
				mana = maxMana;
			}
		}		
	}
}