package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.PassiveSpell;

public class SpellTargetListener extends PassiveListener {

	Map<Spell, List<PassiveSpell>> spells = new HashMap<Spell, List<PassiveSpell>>();
	List<PassiveSpell> anySpell = new ArrayList<PassiveSpell>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			anySpell.add(spell);
		} else {
			String[] split = var.split(",");
			for (String s : split) {
				Spell sp = MagicSpells.getSpellByInternalName(s.trim());
				if (sp != null) {
					List<PassiveSpell> passives = spells.get(sp);
					if (passives == null) {
						passives = new ArrayList<PassiveSpell>();
						spells.put(sp, passives);
					}
					passives.add(spell);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onSpellTarget(SpellTargetEvent event) {
		Spellbook spellbook = MagicSpells.getSpellbook(event.getCaster());
		for (PassiveSpell spell : anySpell) {
			if (spellbook.hasSpell(spell, false)) {
				boolean casted = spell.activate(event.getCaster(), event.getTarget());
				if (casted && spell.cancelDefaultAction()) {
					event.setCancelled(true);
				}
			}
		}
		List<PassiveSpell> list = spells.get(event.getSpell());
		if (list != null) {
			for (PassiveSpell spell : list) {
				if (spellbook.hasSpell(spell, false)) {
					boolean casted = spell.activate(event.getCaster(), event.getTarget());
					if (casted && spell.cancelDefaultAction()) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

}
