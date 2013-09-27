package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;

public class BlockBreakListener extends PassiveListener {

	Map<MagicMaterial, List<PassiveSpell>> types = new HashMap<MagicMaterial, List<PassiveSpell>>();
	List<PassiveSpell> allTypes = new ArrayList<PassiveSpell>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		if (var == null || var.isEmpty()) {
			allTypes.add(spell);
		} else {
			String[] split = var.split(",");
			for (String s : split) {
				s = s.trim();
				MagicMaterial m = MagicSpells.getItemNameResolver().resolveBlock(s);
				if (m != null) {
					List<PassiveSpell> list = types.get(m);
					if (list == null) {
						list = new ArrayList<PassiveSpell>();
						types.put(m, list);
					}
					list.add(spell);
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
		if (allTypes.size() > 0) {			
			for (PassiveSpell spell : allTypes) {
				if (spellbook.hasSpell(spell, false)) {
					spell.activate(event.getPlayer(), event.getBlock().getLocation().add(0.5, 0.5, 0.5));
				}
			}
		}
		if (types.size() > 0) {
			MagicMaterial m = MagicMaterial.fromBlock(event.getBlock());
			if (types.containsKey(m)) {
				List<PassiveSpell> list = types.get(m);		
				for (PassiveSpell spell : list) {
					if (spellbook.hasSpell(spell, false)) {
						spell.activate(event.getPlayer(), event.getBlock().getLocation().add(0.5, 0.5, 0.5));
					}
				}
			}
		}
	}

}
