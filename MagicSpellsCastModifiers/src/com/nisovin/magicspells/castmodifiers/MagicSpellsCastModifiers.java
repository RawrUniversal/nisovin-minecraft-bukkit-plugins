package com.nisovin.magicspells.castmodifiers;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import com.nisovin.magicspells.util.MagicConfig;

public class MagicSpellsCastModifiers extends JavaPlugin implements Listener {
	
	HashMap<Spell, ModifierSet> modifiers = new HashMap<Spell, ModifierSet>();
	ModifierSet manaModifiers;
	
	@Override
	public void onEnable() {
		load();
	}
	
	@Override
	public void onDisable() {
		unload();
	}
	
	@EventHandler
	public void onMagicSpellsLoad(MagicSpellsLoadedEvent event) {
		unload();
		load();
	}
	
	private void load() {
		MagicSpells.debug(1, "Loading cast modifiers...");
		modifiers.clear();
		MagicConfig magicConfig = new MagicConfig(new File(MagicSpells.plugin.getDataFolder(), "config.yml"));
		if (magicConfig.isLoaded()) {
			// load spell modifiers
			for (String spellName : magicConfig.getSpellKeys()) {
				if (magicConfig.contains("spells." + spellName + ".modifiers")) {
					List<String> list = magicConfig.getStringList("spells." + spellName + ".modifiers", null);
					Spell spell = MagicSpells.getSpellByInternalName(spellName);
					if (list != null && list.size() > 0 && spell != null) {
						MagicSpells.debug(2, "Adding modifiers to " + spell.getName() + " spell");
						modifiers.put(spell, new ModifierSet(list, magicConfig.getString("spells." + spellName + ".str-modifier-failed", null)));
					}
				}
			}
			// load mana modifiers
			if (magicConfig.contains("general.mana.modifiers")) {
				List<String> list = magicConfig.getStringList("general.mana.modifiers", null);
				if (list != null && list.size() > 0) {
					MagicSpells.debug(2, "Adding mana modifiers");
					manaModifiers = new ModifierSet(list, "");
				}
			}
		}
		getServer().getPluginManager().registerEvents(this, this);
		if (modifiers.size() > 0) {
			getServer().getPluginManager().registerEvents(new CastListener(this), this);
		}
		if (manaModifiers != null) {
			getServer().getPluginManager().registerEvents(new ManaListener(this), this);
		}
	}
	
	private void unload() {
		HandlerList.unregisterAll((Plugin)this);
	}
	
}
