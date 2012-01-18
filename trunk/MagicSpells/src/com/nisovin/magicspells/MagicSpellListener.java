package com.nisovin.magicspells;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.nisovin.magicspells.events.SpellTargetEvent;

public class MagicSpellListener implements Listener {
		
	public MagicSpellListener(MagicSpells plugin) {
	}

	@EventHandler(event=SpellTargetEvent.class, priority=EventPriority.NORMAL)
	public void onSpellTarget(SpellTargetEvent event) {
		// check if target has notarget permission
		LivingEntity target = event.getTarget();
		if (target instanceof Player) {
			if (((Player)target).hasPermission("magicspells.notarget")) {
				event.setCancelled(true);
			}
		}
	}
	
}
