package com.nisovin.MagicSpells;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import org.bukkit.entity.Player;

public class Spellbook {

	MagicSpells plugin;
	
	private String playerName;
	
	private HashSet<Spell> allSpells = new HashSet<Spell>();	
	private HashMap<Integer,ArrayList<Spell>> itemSpells = new HashMap<Integer,ArrayList<Spell>>();
	private HashMap<Integer,Integer> activeSpells = new HashMap<Integer,Integer>();
	
	public Spellbook(Player player, MagicSpells plugin) {
		this.plugin = plugin;
		this.playerName = player.getName();
		
		// load spells from file
		if (!player.isOp()) {
			loadFromFile();
		} else {
			// give all spells to ops
			for (Spell spell : MagicSpells.spells.values()) {
				addSpell(spell);
			}
		}
	}
	
	private void loadFromFile() {
		try {
			Scanner scanner = new Scanner(new File(plugin.getDataFolder(), "spellbooks/" + playerName.toLowerCase() + ".txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					Spell spell = MagicSpells.spells.get(line);
					if (spell != null) {
						addSpell(spell);
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
	
	public HashSet<Spell> getSpells() {
		return this.allSpells;
	}
	
	public Spell nextSpell(int castItem) {
		Integer i = activeSpells.get(castItem);
		if (i != null) {
			i++;
			ArrayList<Spell> spells = itemSpells.get(castItem);
			if (i >= spells.size()) {
				i = 0;
			}
			activeSpells.put(castItem, i);
			return spells.get(i);
		} else {
			return null;
		}
	}
	
	public Spell getActiveSpell(int castItem) {
		Integer i = activeSpells.get(castItem);
		if (i != null && i != -1) {
			return itemSpells.get(castItem).get(i);
		} else {
			return null;
		}		
	}
	
	public boolean hasSpell(Spell spell) {
		System.out.println("size:"+allSpells.size());
		return allSpells.contains(spell);
	}
	
	public void addSpell(Spell spell) {
		allSpells.add(spell);
		if (spell.canCastWithItem()) {
			int item = spell.getCastItem();
			ArrayList<Spell> temp = itemSpells.get(item);
			if (temp != null) {
				temp.add(spell);
			} else {
				temp = new ArrayList<Spell>();
				temp.add(spell);
				itemSpells.put(item, temp);
				activeSpells.put(item, -1);
			}
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
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(plugin.getDataFolder(), "spellbooks/" + playerName.toLowerCase() + ".txt"), false));
			for (Spell spell : allSpells) {
				writer.append(spell.getInternalName());
				writer.newLine();
			}
			writer.close();
		} catch (Exception e) {
			plugin.getServer().getLogger().severe("Error saving player spellbook: " + playerName);
		}
		
	}
	
}
