package com.nisovin.magicspells.spells.targeted;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;

public class PasteSpell extends TargetedSpell implements TargetedLocationSpell {

	File file;
	int yOffset;
	int maxBlocks;
	boolean pasteAir;
	boolean pasteEntities;
	boolean pasteAtCaster;
	
	public PasteSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		File folder = new File(MagicSpells.plugin.getDataFolder(), "schematics");
		if (!folder.exists()) {
			folder.mkdir();
		}
		String schematic = getConfigString("schematic", "none");
		file = new File(folder, schematic);
		if (!file.exists()) {
			MagicSpells.error("PasteSpell " + spellName + " has non-existant schematic: " + schematic);
		}
		
		yOffset = getConfigInt("y-offset", 0);
		maxBlocks = getConfigInt("max-blocks", 10000);
		pasteAir = getConfigBoolean("paste-air", false);
		pasteEntities = getConfigBoolean("paste-entities", true);
		pasteAtCaster = getConfigBoolean("paste-at-caster", false);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = pasteAtCaster ? player.getLocation().getBlock() : getTargetedBlock(player, range);
			if (target == null) {
				return noTarget(player);
			}
			castAtLocation(target.getLocation(), power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		
		return castAtLocation(target, power);
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		try {
			CuboidClipboard cuboid = SchematicFormat.MCEDIT.load(file);
			cuboid.paste(new EditSession(new BukkitWorld(target.getWorld()), maxBlocks), new Vector(target.getX(), target.getY(), target.getZ()), !pasteAir, pasteEntities);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
