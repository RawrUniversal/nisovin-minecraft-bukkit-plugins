package com.nisovin.magicspells.spells.passive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.materials.MagicMaterial;
import com.nisovin.magicspells.spells.PassiveSpell;

public class RightClickItemListener extends PassiveListener {

	Map<MagicMaterial, List<PassiveSpell>> types = new HashMap<MagicMaterial, List<PassiveSpell>>();
	
	@Override
	public void registerSpell(PassiveSpell spell, PassiveTrigger trigger, String var) {
		String[] split = var.split(",");
		for (String s : split) {
			s = s.trim();
			MagicMaterial mat = MagicSpells.getItemNameResolver().resolveItem(s);
			if (mat != null) {
				List<PassiveSpell> list = types.get(mat);
				if (list == null) {
					list = new ArrayList<PassiveSpell>();
					types.put(mat, list);
				}
				list.add(spell);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!event.hasItem()) return;
		
		ItemStack item = event.getItem();
		if (item == null || item.getType() == Material.AIR) return;
		MagicMaterial m = MagicMaterial.fromItemStack(item);
		if (types.containsKey(m)) {
			Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
			for (PassiveSpell spell : types.get(m)) {
				if (spellbook.hasSpell(spell, false)) {
					spell.activate(event.getPlayer());
				}
			}
		}
	}

}
