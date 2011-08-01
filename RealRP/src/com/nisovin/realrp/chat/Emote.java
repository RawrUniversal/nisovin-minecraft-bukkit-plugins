package com.nisovin.realrp.chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.nisovin.realrp.character.GameCharacter;
import com.nisovin.realrp.character.PlayerCharacter;

public class Emote {

	private int emoteRange = 15;

	private String strToActorNoTarget;
	private String strToActorWithTarget;
	private String strToTarget;
	private String strToNearbyNoTarget;
	private String strToNearbyWithTarget;
	
	public Emote(ConfigurationNode config) {
		emoteRange = config.getInt("range", emoteRange);
		strToActorNoTarget = config.getString("str-to-actor-no-target", "");
		strToActorWithTarget = config.getString("str-to-actor-with-target", "");
		strToTarget = config.getString("str-to-target", "");
		strToNearbyNoTarget = config.getString("str-to-nearby-no-target", "");
		strToNearbyWithTarget = config.getString("str-to-nearby-with-target", "");
	}
	
	public Targetable isTargetable() {
		if (strToActorWithTarget.isEmpty()) {
			return Targetable.TARGET_NOT_ALLOWED;
		} else if (strToActorNoTarget.isEmpty()) {
			return Targetable.TARGET_REQUIRED;
		} else {
			return Targetable.TARGET_ALLOWED;
		}
	}
	
	public EmoteResult use(Player player, String strTarget) {
		// get actor
		PlayerCharacter actor = PlayerCharacter.get(player);
		
		// get target
		GameCharacter target = null;
		Player playerTarget = null;
		List<Entity> entities = player.getNearbyEntities(emoteRange, emoteRange, emoteRange);
		List<Player> nearby = new ArrayList<Player>();
		for (Entity entity : entities) {
			if (entity instanceof Player) {
				Player p = (Player)entity;
				if (target == null && ChatColor.stripColor(p.getDisplayName()).toLowerCase().contains(strTarget)) {
					target = PlayerCharacter.get(p);
					playerTarget = p;
				} else {
					nearby.add(p);
				}
			} else if (entity instanceof HumanEntity) {
				// TODO: allow targeting NPCs
			}
		}
		
		// TODO: make sure we're checking for animated NPCs
		
		// check if we need a target and there isn't one
		if (target == null && strToActorNoTarget.isEmpty()) {
			return EmoteResult.NEEDS_TARGET;
		}
		
		if (target == null) {
			EmoteManager.formatAndSend(player, strToActorNoTarget, actor);
			for (Player p : nearby) {
				EmoteManager.formatAndSend(p, strToNearbyNoTarget, actor);
			}
		} else {
			EmoteManager.formatAndSend(player, strToActorWithTarget, actor, target);
			if (playerTarget != null) {
				EmoteManager.formatAndSend(playerTarget, strToTarget, actor, target);
			}
			for (Player p : nearby) {
				EmoteManager.formatAndSend(p, strToNearbyWithTarget, actor, target);
			}			
		}
		return EmoteResult.OK;
	}
	
	public enum EmoteResult {
		NEEDS_TARGET, OK
	}
	
	public enum Targetable {
		TARGET_REQUIRED, TARGET_ALLOWED, TARGET_NOT_ALLOWED
	}
	
}
