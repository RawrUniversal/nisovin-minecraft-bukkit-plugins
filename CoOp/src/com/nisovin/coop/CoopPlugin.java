package com.nisovin.coop;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CoopPlugin extends JavaPlugin implements Listener {

	static ChatColor chatColor = ChatColor.AQUA;
	static ChatColor highlightColor = ChatColor.DARK_AQUA;
	
	private AltarListener altarListener;
	
	private Map<String, String> partyInvites;
	
	@Override
	public void onEnable() {
		altarListener = new AltarListener(this);
		getServer().getPluginManager().registerEvents(altarListener, this);
		getServer().getPluginManager().registerEvents(new DropListener(this), this);
		getServer().getPluginManager().registerEvents(this, this);
		
		partyInvites = new HashMap<String, String>();
	}
	
	@Override
	public void onDisable() {
		altarListener.removeAllBeacons();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (command.getName().equalsIgnoreCase("coopinvite")) {
			if (sender instanceof Player) {
				Player player = (Player)sender;

				// check if party leader
				Party party = Party.getParty(player);
				if (party != null && !party.isLeader(player)) {
					player.sendMessage(chatColor + "You are not the party leader.");
					return true;
				}
				
				// get target
				if (args.length == 0) {
					player.sendMessage(chatColor + "You must specify a player name.");
					return true;
				}
				Player target = Bukkit.getPlayer(args[0]);
				if (target == null) {
					player.sendMessage(chatColor + "That player could not be found.");
				}
				if (Party.getParty(target) != null) {
					player.sendMessage(chatColor + "That player is already in a party.");
				}
				
				// send invite
				partyInvites.put(target.getName().toLowerCase(), player.getName().toLowerCase());
				target.sendMessage(chatColor + player.getName() + " has invited you to a party.");
				target.sendMessage(chatColor + "Type " + highlightColor + "/accept" + chatColor + " to join the party.");
				player.sendMessage(chatColor + "You have invited " + target.getName() + " to your party.");
				
			}
		}
		return true;
	}
	
	@EventHandler
	public void onPreCommand(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().equalsIgnoreCase("/accept")) {
			Player player = event.getPlayer();
			String inviterName = partyInvites.remove(player.getName().toLowerCase());
			if (inviterName != null) {
				Player inviter = Bukkit.getPlayerExact(inviterName);
				if (inviter != null) {
					Party party = Party.getParty(inviter);
					if (party == null) {
						party = new Party(inviter);
					}
					party.sendMessage(player.getName() + " has joined the party.");
					party.addMember(player);
					player.sendMessage(chatColor + "You have joined the party.");
					event.setCancelled(true);
				}
			}
		}
	}
	
	public Party getParty(Player player) {
		return Party.getParty(player);
	}
	
}
