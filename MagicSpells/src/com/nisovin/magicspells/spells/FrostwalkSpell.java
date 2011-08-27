package com.nisovin.MagicSpells.Spells;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.BuffSpell;
import com.nisovin.MagicSpells.Util.BlockPlatform;

public class FrostwalkSpell extends BuffSpell {
	
	private int size;
	private boolean leaveFrozen;
	
	private HashMap<String,BlockPlatform> frostwalkers;

	public FrostwalkSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		size = config.getInt("spells." + spellName + ".size", 2);
		leaveFrozen = config.getBoolean("spells." + spellName + ".leave-frozen", false);
		
		frostwalkers = new HashMap<String,BlockPlatform>();
		
		addListener(Event.Type.PLAYER_MOVE);
		addListener(Event.Type.PLAYER_QUIT);
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (frostwalkers.containsKey(player.getName())) {
			turnOff(player);
			return PostCastAction.ALREADY_HANDLED;
		} else if (state == SpellCastState.NORMAL) {
			frostwalkers.put(player.getName(), new BlockPlatform(Material.ICE, Material.STATIONARY_WATER, player.getLocation().getBlock().getRelative(0,-1,0), size, !leaveFrozen, "square"));
			startSpellDuration(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public void onPlayerMove(PlayerMoveEvent event) {
		if (frostwalkers.containsKey(event.getPlayer().getName())) {
			Player player = event.getPlayer();
			if (isExpired(player)) {
				turnOff(player);
			} else {
				Block block;
				boolean teleportUp = false;
				if (event.getTo().getY() > event.getFrom().getY() && event.getTo().getY() % 1 > .62 && event.getTo().getBlock().getType() == Material.STATIONARY_WATER && event.getTo().getBlock().getRelative(0,1,0).getType() == Material.AIR) {
					block = event.getTo().getBlock();
					teleportUp = true;
				} else {
					block = event.getTo().getBlock().getRelative(0,-1,0);
				}
				boolean moved = frostwalkers.get(player.getName()).movePlatform(block);
				if (moved) {
					addUse(player);
					chargeUseCost(player);
					if (teleportUp) {
						Location loc = player.getLocation().clone();
						loc.setY(event.getTo().getBlockY()+1);
						player.teleport(loc);
					}
				}
			}
		}
	}
	
	@Override
	public void onPlayerQuit(PlayerQuitEvent event) {
		turnOff(event.getPlayer());
	}
	
	@Override
	protected void turnOff(Player player) {
		BlockPlatform platform = frostwalkers.get(player.getName());
		if (platform != null) {
			platform.destroyPlatform();
			frostwalkers.remove(player.getName());
			sendMessage(player, strFade);
		}
	}
	
	@Override
	protected void turnOff() {
		for (BlockPlatform platform : frostwalkers.values()) {
			platform.destroyPlatform();
		}
		frostwalkers.clear();
	}

}
