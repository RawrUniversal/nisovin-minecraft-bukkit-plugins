package com.nisovin.magicspells.castmodifiers;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.util.MagicConfig;

public class MagicSpellsCastModifiers extends JavaPlugin implements Listener {
	
	private HashMap<Spell, ModifierSet> modifiers = new HashMap<Spell, ModifierSet>();
	
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
		}
		if (modifiers.size() > 0) {
			getServer().getPluginManager().registerEvents(this,this);
		}
	}
	
	private void unload() {
		HandlerList.unregisterAll((Plugin)this);
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onSpellCast(SpellCastEvent event) {
		ModifierSet m = modifiers.get(event.getSpell());
		if (m != null) {
			m.apply(event);
		}
	}

}
