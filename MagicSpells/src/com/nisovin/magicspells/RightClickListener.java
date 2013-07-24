package com.nisovin.magicspells;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.nisovin.magicspells.util.CastItem;

public class RightClickListener implements Listener {

	Map<CastItem, Spell> rightClickCastItems = new HashMap<CastItem, Spell>();
	HashMap<String, Long> lastCast = new HashMap<String, Long>();
	
	public RightClickListener(MagicSpells plugin) {
		for (Spell spell : MagicSpells.spells.values()) {
			CastItem[] items = spell.getRightClickCastItems();
			if (items.length > 0) {
				for (CastItem item : items) {
					Spell old = rightClickCastItems.put(item, spell);
					if (old != null) {
						MagicSpells.error("The spell '" + spell.getInternalName() + "' has same right-click-cast-item as '" + old.getInternalName() + "'!");
					}
				}
			}
		}
	}
	
	public boolean hasRightClickCastItems() {
		return rightClickCastItems.size() > 0;
	}
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
	    if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
	    if (!event.hasItem()) return;	    

	    CastItem castItem = new CastItem(event.getItem());
	    Spell spell = (Spell)this.rightClickCastItems.get(castItem);
	    if (spell == null) return;

	    Player player = event.getPlayer();
		Long lastCastTime = lastCast.get(player.getName());
		if (lastCastTime != null && lastCastTime + MagicSpells.globalCooldown > System.currentTimeMillis()) {
			return;
		} else {
			lastCast.put(player.getName(), System.currentTimeMillis());
		}
	    
	    if (MagicSpells.getSpellbook(player).canCast(spell)) {
	    	spell.cast(event.getPlayer());
	    	event.setCancelled(true);
	    }
	}
	
}
