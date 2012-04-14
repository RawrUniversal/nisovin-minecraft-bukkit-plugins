package com.nisovin.magicspells.spells.buff;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ArmorSpell extends BuffSpell {

	private boolean toggle;
	
	private Material helmet;
	private Material chestplate;
	private Material leggings;
	private Material boots;
	
	private Set<Player> armored;
	
	public ArmorSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		helmet = Material.getMaterial(getConfigString("helmet", "").toUpperCase() + "_HELMET");
		chestplate = Material.getMaterial(getConfigString("chestplate", "").toUpperCase() + "_CHESTPLATE");
		leggings = Material.getMaterial(getConfigString("leggings", "").toUpperCase() + "_LEGGINGS");
		boots = Material.getMaterial(getConfigString("boots", "").toUpperCase() + "_BOOTS");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (toggle && armored.contains(player)) {
			turnOff(player);
			return PostCastAction.ALREADY_HANDLED;
		} else if (state == SpellCastState.NORMAL) {
			PlayerInventory inv = player.getInventory();
			if ((helmet != null && inv.getHelmet() != null) || (chestplate != null && inv.getChestplate() != null) || (leggings != null && inv.getLeggings() != null) || (boots != null && inv.getBoots() != null)) {
				// error
				return PostCastAction.ALREADY_HANDLED;
			}
			
			if (helmet != null) {
				inv.setHelmet(new ItemStack(helmet, 1));
			}
			if (chestplate != null) {
				inv.setChestplate(new ItemStack(chestplate, 1));
			}
			if (leggings != null) {
				inv.setLeggings(new ItemStack(leggings, 1));
			}
			if (boots != null) {
				inv.setBoots(new ItemStack(boots, 1));
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getSlotType() == SlotType.ARMOR && event.getWhoClicked() instanceof Player) {
			Player p = (Player)event.getWhoClicked();
			if (armored.contains(p)) {
				event.setCancelled(true);
			}
		}
	}
	
	@Override
	public void turnOff(Player player) {
		super.turnOff(player);
		if (armored.contains(player)) {
			armored.remove(player);
			PlayerInventory inv = player.getInventory();
			if (helmet != null && inv.getHelmet() != null && inv.getHelmet().getType() == helmet) {
				inv.setHelmet(null);
			}
			if (chestplate != null && inv.getChestplate() != null && inv.getChestplate().getType() == chestplate) {
				inv.setChestplate(null);
			}
			if (leggings != null && inv.getLeggings() != null && inv.getLeggings().getType() == leggings) {
				inv.setLeggings(null);
			}
			if (boots != null && inv.getBoots() != null && inv.getBoots().getType() == boots) {
				inv.setBoots(null);
			}
		}
	}

	@Override
	protected void turnOff() {
		for (Player p : new HashSet<Player>(armored)) {
			if (p.isOnline()) {
				turnOff(p);
			}
		}
	}

}
