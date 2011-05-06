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
	
	private HashMap<Integer,ArrayList<Spell>> spells;
	private HashMap<Integer,Integer> activeSpells;
	
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
						if (spell.canCastWithItem()) {
							int castItem = spell.getCastItem();
							if (spells.containsKey(castItem)) {
								spells.get(castItem).add(spell);
							} else {
								ArrayList<Spell> temp = new ArrayList<Spell>();
								temp.add(spell);
								spells.put(castItem, temp);
								activeSpells.put(castItem, -1);
							}
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
	
	public Spell nextSpell(int castItem) {
		Integer i = activeSpells.get(castItem);
		if (i != null) {
			i++;
			ArrayList<Spell> spells = spells.get(castItem);
			if (i > spells.size()) {
				i = 0;
			}
			return spells.get(i);
		} else {
			return null;
		}
	}
	
	/*public Spell nextSpell(SpellType type) {
		if (type == SpellType.WAND_SPELL) {
			activeWandSpell++;
			if (activeWandSpell >= wandSpells.size()) {
				activeWandSpell = 0;
			}
			return wandSpells.get(activeWandSpell);
		} else {
			return null;
		}
	}*/
	
	public Spell getActiveSpell(int castItem) {
		Integer i = activeSpells.get(castItem);
		if (i != null && i != -1) {
			return spells.get(castItem).get(i);
		} else {
			return null;
		}		
	}
	
	/*public Spell getActiveSpell(SpellType type) {
		if (type == SpellType.WAND_SPELL) {
			if (activeWandSpell == -1) {
				return null;
			} else {
				return wandSpells.get(activeWandSpell);
			} 
		} else {
			return null;
		}		
	}*/
	
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
