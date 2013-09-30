package com.nisovin.magicspells;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.nisovin.magicspells.Spell.PostCastAction;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.util.IntMap;

public class MagicXpHandler implements Listener {

	MagicSpells plugin;
	
	Map<String, IntMap<String>> xp = new HashMap<String, IntMap<String>>();
	Set<String> dirty = new HashSet<String>();
	Map<String, String> currentWorld = new HashMap<String, String>();
	
	Map<String, List<Spell>> spellSchoolRequirements = new HashMap<String, List<Spell>>();
	
	public MagicXpHandler(MagicSpells plugin) {
		this.plugin = plugin;
		
		for (Spell spell : MagicSpells.spells()) {
			Map<String, Integer> xpRequired = spell.getXpRequired();
			if (xpRequired != null) {
				for (String school : xpRequired.keySet()) {
					List<Spell> list = spellSchoolRequirements.get(school.toLowerCase());
					if (list == null) {
						list = new ArrayList<Spell>();
						spellSchoolRequirements.put(school.toLowerCase(), list);
					}
					list.add(spell);
				}
			}
		}
		
		for (Player player : Bukkit.getOnlinePlayers()) {
			load(player);
		}
		
		MagicSpells.scheduleRepeatingTask(new Runnable() {
			public void run() {
				saveAll();
			}
		}, 60 * 20, 60 * 20);
		
		MagicSpells.registerEvents(this);
	}
	
	@EventHandler
	public void onCast(SpellCastedEvent event) {
		if (event.getPostCastAction() == PostCastAction.ALREADY_HANDLED) return;
		
		// get player xp
		IntMap<String> playerXp = xp.get(event.getCaster().getName());
		if (playerXp == null) {
			playerXp = new IntMap<String>();
			xp.put(event.getCaster().getName(), playerXp);
		}
		
		// grant xp
		Map<String, Integer> xpGranted = event.getSpell().getXpGranted();
		for (String school : xpGranted.keySet()) {
			playerXp.increment(school.toLowerCase(), xpGranted.get(school));
		}
		
		// get spells to check if learned
		Set<Spell> toCheck = new HashSet<Spell>();
		for (String school : xpGranted.keySet()) {
			List<Spell> list = spellSchoolRequirements.get(school.toLowerCase());
			if (list != null) {
				for (Spell spell : list) {
					toCheck.add(spell);
				}
			}
		}
		
		// check for new learned spells
		if (toCheck.size() > 0) {
			Spellbook spellbook = MagicSpells.getSpellbook(event.getCaster());
			for (Spell spell : toCheck) {
				if (!spellbook.hasSpell(spell, false) && spellbook.canLearn(spell)) {
					Map<String, Integer> xpRequired = spell.getXpRequired();
					if (xpRequired != null) {
						boolean learn = true;
						for (String school : xpRequired.keySet()) {
							if (playerXp.get(school.toLowerCase()) < xpRequired.get(school)) {
								learn = false;
								break;
							}
						}
						if (learn) {
							spellbook.addSpell(spell);
							MagicSpells.sendMessage(event.getCaster(), spell.getStrXpLearned());
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		currentWorld.put(event.getPlayer().getName(), event.getPlayer().getWorld().getName());
		dirty.remove(event.getPlayer().getName());
		load(event.getPlayer());
	}

	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent event) {
		if (plugin.separatePlayerSpellsPerWorld) {
			Player player = event.getPlayer();
			if (dirty.contains(player.getName())) {
				save(player);
			}
			currentWorld.put(player.getName(), player.getWorld().getName());
			load(player);
			dirty.remove(player.getName());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (dirty.contains(player.getName())) {
			save(player);
		}
		xp.remove(player.getName());
		dirty.remove(player.getName());
		currentWorld.remove(player.getName());
	}
	
	public void load(Player player) {
		File folder = new File(plugin.getDataFolder(), "xp");
		if (!folder.exists()) folder.mkdirs();
		if (plugin.separatePlayerSpellsPerWorld) {
			String world = currentWorld.get(player.getName());
			if (world == null) world = player.getWorld().getName();
			folder = new File(folder, world);
			if (!folder.exists()) folder.mkdirs();
		}
		File file = new File(folder, player.getName().toLowerCase());
		if (file.exists()) {
			YamlConfiguration conf = new YamlConfiguration();
			try {
				conf.load(file);
				IntMap<String> playerXp = new IntMap<String>();
				for (String school : conf.getKeys(false)) {
					playerXp.put(school.toLowerCase(), conf.getInt(school, 0));
				}
				xp.put(player.getName(), playerXp);
			} catch (Exception e) {
				MagicSpells.error("Error while loading player XP for player " + player.getName());
				MagicSpells.handleException(e);
			}
		}
	}
	
	public void saveAll() {
		for (String playerName : dirty) {
			Player player = Bukkit.getPlayerExact(playerName);
			if (player != null) {
				save(player);
			} else {
				String world = currentWorld.get(playerName);
				save(playerName, world);
			}
		}
		dirty.clear();
	}
	
	public void save(Player player) {
		String world = currentWorld.get(player.getName());
		if (world == null) world = player.getWorld().getName();
		save(player.getName(), world);
	}
	
	public void save(String player, String world) {
		File folder = new File(plugin.getDataFolder(), "xp");
		if (!folder.exists()) folder.mkdirs();
		if (plugin.separatePlayerSpellsPerWorld) {
			if (world == null) return;
			folder = new File(folder, world);
			if (!folder.exists()) folder.mkdirs();
		}
		File file = new File(folder, player.toLowerCase());
		
		YamlConfiguration conf = new YamlConfiguration();
		IntMap<String> playerXp = xp.get(player);
		if (playerXp != null) {
			for (String school : playerXp.keySet()) {
				conf.set(school.toLowerCase(), playerXp.get(school));
			}
		}
		
		try {
			conf.save(file);
		} catch (Exception e) {
			MagicSpells.error("Error while saving player XP for player " + player);
			MagicSpells.handleException(e);
		}
	}
	
}
