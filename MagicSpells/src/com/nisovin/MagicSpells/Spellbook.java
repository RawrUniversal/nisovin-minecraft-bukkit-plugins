package com.nisovin.MagicSpells;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

import org.bukkit.entity.Player;

public class Spellbook {

	private MagicSpells plugin;
	
	private static PermissionHandler permissionHandler = null;
	
	private String playerName;
	
	private TreeSet<Spell> allSpells = new TreeSet<Spell>();	
	private HashMap<Integer,ArrayList<Spell>> itemSpells = new HashMap<Integer,ArrayList<Spell>>();
	private HashMap<Integer,Integer> activeSpells = new HashMap<Integer,Integer>();
	
	public Spellbook(Player player, MagicSpells plugin) {
		this.plugin = plugin;
		this.playerName = player.getName();
		
		// load spells from file
		if (!player.isOp() || !MagicSpells.opsHaveAllSpells) {
			loadFromFile();
		} else {
			// give all spells to ops
			for (Spell spell : MagicSpells.spells.values()) {
				addSpell(spell);
			}
		}
		
		// add spells granted by permissions
		if (permissionHandler != null) {
			for (Spell spell : MagicSpells.spells.values()) {
				if (!hasSpell(spell) && permissionHandler.has(player, "magicspells.grant." + spell.getInternalName())) {
					addSpell(spell);
				}
			}
		}
		
		// sort spells
		for (ArrayList<Spell> spells : itemSpells.values()) {
			Collections.sort(spells);
		}
	}
	
	public boolean canLearn(Spell spell) {
		if (permissionHandler == null) {
			return true;
		} else {
			return permissionHandler.has(MagicSpells.plugin.getServer().getPlayer(playerName), "magicspells.learn." + spell.getInternalName());
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
	
	public Set<Spell> getSpells() {
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
		allSpells.remove(spell);
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
	
	public static void initPermissions() {
		Plugin permissionsPlugin = MagicSpells.plugin.getServer().getPluginManager().getPlugin("Permissions");

		if (permissionHandler == null) {
			if (permissionsPlugin != null) {
				permissionHandler = ((Permissions) permissionsPlugin).getHandler();
			} else {
				MagicSpells.plugin.getServer().getLogger().info("MagicSpells: enable-permissions enabled, but no Permissions plugin found.");
			}
		}
	}
	
}
