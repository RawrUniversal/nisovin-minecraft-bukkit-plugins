package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class TelekinesisSpell extends InstantSpell {

	private static final String SPELL_NAME = "telekinesis";

	private String strNoTarget;
	
	HashSet<Byte> transparent;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new TelekinesisSpell(config, spellName));
		}
	}
	
	public TelekinesisSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You must target a switch or button.");
		
		transparent = new HashSet<Byte>();
		transparent.add((byte)Material.AIR.getId());
		transparent.add((byte)Material.REDSTONE_WIRE.getId());
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(transparent, range>0?range:100);
			if (target == null) {
				// fail
				sendMessage(player, strNoTarget);
				return true;
			} else if (target.getType() == Material.LEVER || target.getType() == Material.STONE_BUTTON) {
				target.setData((byte) (target.getData() ^ 0x8));
			} else if (target.getType() == Material.WOOD_PLATE || target.getType() == Material.STONE_PLATE) {
				target.setData((byte) (target.getData() ^ 0x1));				
			} else {
				// fail
				sendMessage(player, strNoTarget);
				return true;
			}
		}
		return false;
	}
}