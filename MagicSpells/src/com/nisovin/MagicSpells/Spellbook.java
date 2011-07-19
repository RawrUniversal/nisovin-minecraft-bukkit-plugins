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
	
	private Player player;
	private String playerName;
	
	private TreeSet<Spell> allSpells = new TreeSet<Spell>();
	private HashMap<Integer,ArrayList<Spell>> itemSpells = new HashMap<Integer,ArrayList<Spell>>();
	private HashMap<Integer,Integer> activeSpells = new HashMap<Integer,Integer>();
	private HashMap<Spell,Integer> customBindings = new HashMap<Spell,Integer>();
	
	public Spellbook(Player player, MagicSpells plugin) {
		MagicSpells.debug("Loading player spell list: " + player.getName());
		this.plugin = plugin;
		this.player = player;
		this.playerName = player.getName();
		
		// load spells from file
		loadFromFile();
		
		// give all spells to ops
		if (player.isOp() && MagicSpells.opsHaveAllSpells) {
			MagicSpells.debug("  Op, granting all spells...");
			for (Spell spell : MagicSpells.spells.values()) {
				if (!allSpells.contains(spell)) {
					addSpell(spell);
				}
			}
		}
		
		// add spells granted by permissions
		addGrantedSpells();
		
		// sort spells or pre-select if just one
		for (Integer i : itemSpells.keySet()) {
			ArrayList<Spell> spells = itemSpells.get(i);
			if (spells.size() == 1) {
				activeSpells.put(i, 0);
			} else {
				Collections.sort(spells);
			}
		}
	}
	
	public void addGrantedSpells() {
		MagicSpells.debug("  Adding granted spells...");
		boolean added = false;
		boolean grantedAll = player.hasPermission("magicspells.grant.*");
		for (Spell spell : MagicSpells.spells.values()) {
			MagicSpells.debug("    Checking spell " + spell.getInternalName() + "...");
			if (!hasSpell(spell)) {
				String perm = "magicspells.grant." + spell.getInternalName();
				if (grantedAll || player.hasPermission(perm) || (permissionHandler != null && permissionHandler.has(player, perm))) {
					addSpell(spell);
					added = true;
				}
			}
		}
		if (added) {
			save();
		}
	}	
	
	public boolean canLearn(Spell spell) {
		if (permissionHandler == null) {
			return player.hasPermission("magicspells.learn." + spell.getInternalName()) || player.hasPermission("magicspells.learn.*");
		} else {
			return permissionHandler.has(MagicSpells.plugin.getServer().getPlayer(playerName), "magicspells.learn." + spell.getInternalName());
		}
	}
	
	public boolean canCast(Spell spell) {
		if (permissionHandler == null) {
			return player.hasPermission("magicspells.cast." + spell.getInternalName()) || player.hasPermission("magicspells.cast.*");
		} else {
			return permissionHandler.has(MagicSpells.plugin.getServer().getPlayer(playerName), "magicspells.cast." + spell.getInternalName());
		}
	}
	
	public boolean canTeach(Spell spell) {
		if (permissionHandler == null) {
			return player.hasPermission("magicspells.cast." + spell.getInternalName()) || player.hasPermission("magicspells.teach.*");
		} else {
			return permissionHandler.has(MagicSpells.plugin.getServer().getPlayer(playerName), "magicspells.teach." + spell.getInternalName());
		}
	}
	
	private void loadFromFile() {
		try {
			MagicSpells.debug("  Loading spells from player file...");
			Scanner scanner = new Scanner(new File(plugin.getDataFolder(), "spellbooks/" + playerName.toLowerCase() + ".txt"));
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (!line.equals("")) {
					if (!line.contains(":")) {
						Spell spell = MagicSpells.spells.get(line);
						if (spell != null) {
							addSpell(spell);
						}
					} else {
						String[] data = line.split(":");
						Spell spell = MagicSpells.spells.get(data[0]);
						if (spell != null && data[1].matches("^-?[0-9]+$")) {
							addSpell(spell, Integer.parseInt(data[1]));
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
	
	public Set<Spell> getSpells() {
		return this.allSpells;
	}
	
	public Spell nextSpell(int castItem) {
		Integer i = activeSpells.get(castItem);
		if (i != null) {
			ArrayList<Spell> spells = itemSpells.get(castItem);
			if (spells.size() > 1 || i.equals(-1)) {
				i++;
				if (i >= spells.size()) {
					i = 0;
				}
				activeSpells.put(castItem, i);
				return spells.get(i);
			} else {
				return null;
			}
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
		addSpell(spell, 0);
	}
	
	public void addSpell(Spell spell, int castItem) {
		MagicSpells.debug("    Added spell: " + spell.getInternalName());
		allSpells.add(spell);
		if (spell.canCastWithItem()) {
			int item = spell.getCastItem();
			if (castItem != 0) {
				item = castItem;
				customBindings.put(spell, castItem);
			} else if (MagicSpells.ignoreDefaultBindings) {
				return; // no cast item provided and ignoring default, so just stop here
			}
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
		if (customBindings.containsKey(spell)) {
			item = customBindings.remove(spell);
		}
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
				if (customBindings.containsKey(spell)) {
					writer.append(":" + customBindings.get(spell));
				}
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
