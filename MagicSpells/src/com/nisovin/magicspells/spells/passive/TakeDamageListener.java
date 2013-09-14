package com.nisovin.magicspells.spells.passive;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.PassiveSpell;

public class TakeDamageListener implements PassiveListener, Listener {

	Map<EntityDamageEvent.DamageCause, List<PassiveSpell>> damageCauses; 
	
	@Override
	public void registerSpell(PassiveSpell spell, String var) {
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		
		List<PassiveSpell> causeSpells = damageCauses.get(event.getCause());
		if (causeSpells != null && causeSpells.size() > 0) {
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			LivingEntity attacker = null;
			if (event instanceof EntityDamageByEntityEvent) {
				Entity e = ((EntityDamageByEntityEvent)event).getDamager();
				if (e instanceof LivingEntity) {
					attacker = (LivingEntity)e;
				} else if (e instanceof Projectile) {
					attacker = ((Projectile)e).getShooter();
				}
			}
			for (PassiveSpell spell : causeSpells) {
				if (spellbook.hasSpell(spell)) {
					spell.activate(player, attacker);
				}
			}
		}
	}

}
