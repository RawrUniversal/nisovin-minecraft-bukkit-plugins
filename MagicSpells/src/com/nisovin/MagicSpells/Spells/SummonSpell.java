package com.nisovin.MagicSpells.Spells;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.ChanneledSpell;

public class SummonSpell extends ChanneledSpell {

	private boolean requireExactName;
	private boolean requireAcceptance;
	private int maxAcceptDelay;
	private String acceptCommand;
	private String strUsage;
	private String strNoTarget;
	private String strSummonPending;
	private String strSummonAccepted;
	private String strSummonExpired;
	
	private HashMap<Player,Location> pendingSummons;
	private HashMap<Player,Long> pendingTimes;
	
	public SummonSpell(Configuration config, String spellName) {
		super(config, spellName);		
		
		requireExactName = getConfigBoolean(config, "require-exact-name", false);
		requireAcceptance = getConfigBoolean(config, "require-acceptance", true);
		maxAcceptDelay = getConfigInt(config, "max-accept-delay", 90);
		acceptCommand = getConfigString(config, "accept-command", "accept");
		strUsage = getConfigString(config, "str-usage", "Usage: /cast summon <playername>, or /cast summon \nwhile looking at a sign with a player name on the first line.");
		strNoTarget = getConfigString(config, "str-no-target", "Target player not found.");
		strSummonPending = getConfigString(config, "str-summon-pending", "You are being summoned! Type /accept to teleport.");
		strSummonAccepted = getConfigString(config, "str-summon-accepted", "You have been summoned.");
		strSummonExpired = getConfigString(config, "str-summon-expired", "The summon has expired.");

		if (requireAcceptance) {
			addListener(Event.Type.PLAYER_COMMAND_PREPROCESS);
			pendingSummons = new HashMap<Player,Location>();
			pendingTimes = new HashMap<Player,Long>();
		}
		
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get target name
			String targetName = "";
			if (args != null && args.length > 0) {
				targetName = args[0];
			} else {
				Block block = player.getTargetBlock(null, 10);
				if (block != null && (block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST)) {
					Sign sign = (Sign)block.getState();
					targetName = sign.getLine(0);
				}
			}
			
			// check usage
			if (targetName.equals("")) {
				// fail -- show usage
				sendMessage(player, strUsage);
				return true;
			}
			
			// get player
			Player target = null;
			if (requireExactName) {
				target = Bukkit.getServer().getPlayer(targetName);
				if (target != null && !target.getName().equalsIgnoreCase(targetName)) {
					target = null;
				}
			} else {
				List<Player> players = Bukkit.getServer().matchPlayer(targetName);
				if (players != null && players.size() == 1) {
					target = players.get(0);
				}
			}
			if (target == null) {
				// fail -- no player target
				sendMessage(player, strNoTarget);
				return true;
			}
			
			// start channel
			boolean success = addChanneler(target.getName(), player);
			if (!success) {
				// failed to channel -- don't charge stuff or cooldown
				return true;
			}
			
		}
		return false;
	}

	@Override
	protected void finishSpell(String key, Location location) {
		Player target = Bukkit.getServer().getPlayer(key);
		if (target != null) {
			if (requireAcceptance) {
				pendingSummons.put(target, location);
				pendingTimes.put(target, System.currentTimeMillis());
				sendMessage(target, strSummonPending);
			} else {
				target.teleport(location);
				sendMessage(target, strSummonAccepted);
			}
		}
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().equalsIgnoreCase("/" + acceptCommand) && pendingSummons.containsKey(event.getPlayer())) {
			Player player = event.getPlayer();
			if (maxAcceptDelay > 0 && pendingTimes.get(player) + maxAcceptDelay*1000 < System.currentTimeMillis()) {
				// waited too long
				sendMessage(player, strSummonExpired);
			} else {
				// all ok, teleport
				player.teleport(pendingSummons.get(player));
				sendMessage(player, strSummonAccepted);
			}
			pendingSummons.remove(player);
			pendingTimes.remove(player);
			event.setCancelled(true);
		}
	}

}
