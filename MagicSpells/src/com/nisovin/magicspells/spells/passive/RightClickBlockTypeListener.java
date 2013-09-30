package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;

public class RightClickBlockTypeListener extends PassiveListener {

	Map<MagicMaterial, List<PassiveSpell>> types = new HashMap<MagicMaterial, List<PassiveSpell>>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
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
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		MagicMaterial m = MagicMaterial.fromBlock(event.getClickedBlock());
		if (types.containsKey(m)) {
			Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
			for (PassiveSpell spell : types.get(m)) {
				if (spellbook.hasSpell(spell, false)) {
					spell.activate(event.getPlayer(), event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5));
				}
			}
		}
	}

}
