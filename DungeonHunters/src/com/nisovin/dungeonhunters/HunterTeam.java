package com.nisovin.dungeonhunters;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scoreboard.Team;

public class HunterTeam {

	String name;
	ChatColor chatColor;
	Color armorColor;
	Team scoreboardTeam;
	
	Set<String> players;
	Set<String> removedPlayers;
	
	public HunterTeam(String name, ChatColor chatColor, Color armorColor) {
		this.name = name;
		this.chatColor = chatColor;
		this.armorColor = armorColor;
		
		this.scoreboardTeam = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(name);
		this.scoreboardTeam.setDisplayName(chatColor + name);
		this.scoreboardTeam.setPrefix(chatColor.toString());
		this.scoreboardTeam.setCanSeeFriendlyInvisibles(true);
		this.scoreboardTeam.setAllowFriendlyFire(false);
		
		this.players = new HashSet<String>();
		this.removedPlayers = new HashSet<String>();
	}
	
	public int size() {
		return players.size();
	}
	
	public String getName() {
		return name;
	}
	
	public ChatColor getChatColor() {
		return chatColor;
	}
	
	public void addPlayer(Player player) {
		players.add(player.getName());
		scoreboardTeam.addPlayer(player);
		
		player.getInventory().clear();
		
		ItemStack helm = new ItemStack(Material.LEATHER_HELMET);
		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		
		ItemStack[] toColor = { helm, chest, legs, boots };
		for (ItemStack item : toColor) {
			item.addUnsafeEnchantment(Enchantment.DURABILITY, 11);
			LeatherArmorMeta meta = (LeatherArmorMeta)item.getItemMeta();
			meta.setColor(armorColor);
			item.setItemMeta(meta);
		}
		
		player.getInventory().setHelmet(helm);
		player.getInventory().setChestplate(chest);
		player.getInventory().setLeggings(legs);
		player.getInventory().setBoots(boots);
	}
	
	public void removePlayer(Player player) {
		boolean removed = players.remove(player.getName());
		if (removed) {
			scoreboardTeam.removePlayer(player);
			removedPlayers.add(player.getName());
		}
	}
	
}
