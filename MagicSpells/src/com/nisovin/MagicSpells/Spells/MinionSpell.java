package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.server.EntityCreature;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class MinionSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "minion";
	
	private CreatureType[] creatureTypes;
	private int[] chances;
	private boolean preventCombust;
	private boolean targetPlayers;
	
	private HashMap<String,LivingEntity> minions;
	private HashMap<String,LivingEntity> targets;
	Random random;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new MinionSpell(config, spellName));
		}
	}
	
	public MinionSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		List<String> c = config.getStringList("spells." + spellName + ".mob-chances", null);
		if (c == null) {
			c = new ArrayList<String>();
		}
		if (c.size() == 0) {
			c.add("Zombie 100");
		}
		creatureTypes = new CreatureType[c.size()];
		chances = new int[c.size()];
		for (int i = 0; i < c.size(); i++) {
			String[] data = c.get(i).split(" ");
			CreatureType creatureType = CreatureType.fromName(data[0]);
			int chance = 0;
			if (creatureType != null) {
				try {
					chance = Integer.parseInt(data[1]);
				} catch (NumberFormatException e) {
				}
			}
			creatureTypes[i] = creatureType;
			chances[i] = chance;
		}
		preventCombust = config.getBoolean("spells." + spellName + ".prevent-sun-burn", true);
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		
		minions = new HashMap<String,LivingEntity>();
		targets = new HashMap<String,LivingEntity>();
		random = new Random();
		
		addListener(Event.Type.ENTITY_TARGET);
		addListener(Event.Type.ENTITY_DAMAGE);
		if (preventCombust) {
			addListener(Event.Type.ENTITY_COMBUST);			
		}
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (minions.containsKey(player.getName())) {
			LivingEntity minion = minions.get(player.getName());
			if (!minion.isDead()) { // don't toggle off if the minion is dead
				turnOff(player);
				return true;
			}
		} 
		if (state == SpellCastState.NORMAL) {
			CreatureType creatureType = null;
			int num = random.nextInt(100);
			int n = 0;
			for (int i = 0; i < creatureTypes.length; i++) {
				if (num < chances[i] + n) {
					creatureType = creatureTypes[i];
					break;
				} else {
					n += chances[i];
				}
			}
			System.out.println(creatureType);
			if (creatureType != null) {
				// get spawn location TODO: improve this
				Location loc = null;
				loc = player.getLocation();
				loc.setX(loc.getX()-1);
				
				// spawn creature
				LivingEntity minion = player.getWorld().spawnCreature(loc, creatureType);
				minions.put(player.getName(), minion);
				targets.put(player.getName(), null);
				startSpellDuration(player);
			} else {
				// fail -- no creature found
			}
		}
		return false;
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (!event.isCancelled() && minions.size() > 0 ) {	
			if (event.getTarget() instanceof Player) {
				// a monster is trying to target a player
				Player player = (Player)event.getTarget();
				LivingEntity minion = minions.get(player.getName());
				if (minion != null && minion.getEntityId() == event.getEntity().getEntityId()) {
					// the targeted player owns the minion
					if (isExpired(player)) {
						// spell is expired
						turnOff(player);
						return;
					}
					// check if the player has a current target
					LivingEntity target = targets.get(player.getName());
					if (target != null) {
						// player has a target
						if (target.isDead()) {
							// the target is dead, so remove that target
							targets.put(player.getName(), null);
							event.setCancelled(true);
						} else {
							// send the minion after the player's target
							event.setTarget(targets.get(player.getName()));
							addUse(player);
							chargeUseCost(player);
						}
					} else {
						// player doesn't have a target, so just order the minion to follow
						event.setCancelled(true);
						double distSq = minion.getLocation().toVector().distanceSquared(player.getLocation().toVector());
						if (distSq > 3*3) {
							// minion is too far, tell him to move closer
							EntityCreature entity = ((CraftCreature)minion).getHandle();
							entity.pathEntity = entity.world.findPath(entity, ((CraftPlayer)player).getHandle(), 16.0F);
						} 
					}
				} else if (!targetPlayers && minions.containsValue(event.getEntity())) {
					// player doesn't own minion, but it is an owned minion and pvp is off, so cancel
					event.setCancelled(true);
				}				
			} else if (event.getReason() == TargetReason.FORGOT_TARGET && minions.containsValue(event.getEntity())) {
				// forgetting target but it's a minion, don't let them do that! (probably a spider going passive)
				event.setCancelled(true);
			}
		}
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (!event.isCancelled() && event instanceof EntityDamageByEntityEvent && event.getEntity() instanceof LivingEntity) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
			if (evt.getDamager() instanceof Player) {
				Player p = (Player)evt.getDamager();
				if (minions.containsKey(p.getName())) {
					if (isExpired(p)) {
						turnOff(p);
						return;
					}
					LivingEntity target = (LivingEntity)event.getEntity();
					((Creature)minions.get(p.getName())).setTarget(target);
					targets.put(p.getName(), target);
					addUse(p);
					chargeUseCost(p);
				}
			}
		}
	}	
	
	@Override
	public void onEntityCombust(EntityCombustEvent event) {
		if (!event.isCancelled() && minions.containsValue(event.getEntity())) {
			event.setCancelled(true);
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		LivingEntity minion = minions.get(player.getName());
		if (minion != null && !minion.isDead()) {
			minion.setHealth(0);
			sendMessage(player, strFade);
		}
		minions.remove(player.getName());
		targets.remove(player.getName());
	}
	
	@Override
	protected void turnOff() {
		for (LivingEntity minion : minions.values()) {
			minion.setHealth(0);
		}
		minions.clear();
		targets.clear();
	}
	
}