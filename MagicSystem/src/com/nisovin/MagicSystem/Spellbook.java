package com.nisovin.MagicSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.bukkit.entity.Player;

public class Spellbook {

	MagicSystem plugin;
	
	private String playerName;
	
	private HashSet<Spell> allSpells = new HashSet<Spell>();
	private ArrayList<WandSpell> wandSpells = new ArrayList<WandSpell>();
	private int activeWandSpell = -1;
	
	public Spellbook(Player player, MagicSystem plugin) {
		this.plugin = plugin;
		this.playerName = player.getName();
		
		// load spells from file
		try {
			Scanner scanner = new Scanner(new File(plugin.getDataFolder(), "spellbooks/" + playerName + ".txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				System.out.println(line);
				if (!line.equals("")) {
					Spell spell = MagicSystem.spells.get(line);
					if (spell != null) {
						allSpells.add(spell);
						if (spell instanceof WandSpell) {
							wandSpells.add((WandSpell)spell);
						}
					}
				}
			}
			scanner.close();
		} catch (Exception e) {			
		}
	}
	
	public Spell getSpellByName(String spellName) {
		for (Spell spell : allSpells) {
			if (spell.getName().equalsIgnoreCase(spellName)) {
				return spell;
			}
		}
		return null;
	}
	
	public Spell nextSpell(SpellType type) {
		if (type == SpellType.WAND_SPELL) {
			activeWandSpell++;
			if (activeWandSpell >= wandSpells.size()) {
				activeWandSpell = 0;
			}
			return wandSpells.get(activeWandSpell);
		} else {
			return null;
		}
	}
	
	public Spell getActiveSpell(SpellType type) {
		if (type == SpellType.WAND_SPELL) {
			if (activeWandSpell == -1) {
				return null;
			} else {
				return wandSpells.get(activeWandSpell);
			} 
		} else {
			return null;
		}		
	}
	
	public void addSpell(Spell spell) {
		if (spell instanceof WandSpell) {
			wandSpells.add((WandSpell)spell);
		}
	}
	
	public void removeSpell(Spell spell) {
		if (spell instanceof WandSpell) {
			wandSpells.remove((WandSpell)spell);
		}
	}
	
	public enum SpellType {
		WAND_SPELL
	}
	
}
