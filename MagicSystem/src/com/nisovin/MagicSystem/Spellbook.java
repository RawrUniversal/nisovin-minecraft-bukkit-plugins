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
	private HashMap<Integer,ArrayList<Spell>> itemSpells;
	private HashMap<Integer,Integer> activeSpells;
	
	public Spellbook(Player player, MagicSystem plugin) {
		this.plugin = plugin;
		this.playerName = player.getName();
		
		// load spells from file
		if (!player.isOp()) {
			loadFromFile();
		} else {
			// give all spells to ops
			for (Spell spell : MagicSystem.spells) {
				allSpells.add(spell);
				addSpell(spell);
			}
		}
	}
	
	private void loadFromFile() {
		try {
			Scanner scanner = new Scanner(new File(plugin.getDataFolder(), "spellbooks/" + playerName.toLowerCase() + ".txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				System.out.println(line);
				if (!line.equals("")) {
					Spell spell = MagicSystem.spells.get(line);
					if (spell != null) {
						allSpells.add(spell);
						if (spell.canCastWithItem()) {
							addSpell(spell);
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
	
	public Spell getActiveSpell(int castItem) {
		Integer i = activeSpells.get(castItem);
		if (i != null && i != -1) {
			return spells.get(castItem).get(i);
		} else {
			return null;
		}		
	}
	
	public boolean hasSpell(Spell spell) {
		return allSpells.contains(spell);
	}
	
	public void addSpell(Spell spell) {
		int item = spell.getCastItem();
		ArrayList<Spell> temp = itemSpells.get(item);
		if (temp != null) {
			temp.add(spell);
		} else {
			temp = new ArrayList<Spell>();
			itemSpells.put(item, temp);
			activeSpells.put(item, -1);
		}
	}
	
	public void removeSpell(Spell spell) {
		int item = spell.getCastItem();
		ArrayList<Spell> temp = itemSpells.get(item);
		if (temp != null) {
			temp.remove(spell);
			if (temp.size() == 0) {
				itemSpells.remove(item);
				activeSpells.remove(item);
			} else {
				activeSpells.put(item, -1);
			}
		}
	}
	
	public void save() {
		// TODO
	}
	
	public enum SpellType {
		WAND_SPELL
	}
	
}
