package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.ItemNameResolver.ItemTypeAndData;
import com.nisovin.magicspells.util.MagicConfig;

public class PulseSpell extends TargetedLocationSpell {

	private int totalPulses;
	private int interval;
	private int typeId;
	private byte data;
	private boolean unbreakable;
	private List<String> spellNames;
	private List<TargetedLocationSpell> spells;
	
	private HashMap<Block, Pulser> pulsers;
	
	public PulseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		totalPulses = getConfigInt("total-pulses", 5);
		interval = getConfigInt("interval", 30);
		ItemTypeAndData type = MagicSpells.getItemNameResolver().resolve(getConfigString("block-type", "diamond_block"));
		typeId = type.id;
		data = (byte)type.data;
		unbreakable = getConfigBoolean("unbreakable", false);
		spellNames = getConfigStringList("spells", null);
		
		pulsers = new HashMap<Block, Pulser>();
	}
	
	@Override
	public void initialize() {
		super.initialize();
		spells = new ArrayList<TargetedLocationSpell>();
		if (spellNames != null && spellNames.size() > 0) {
			for (String spellName : spellNames) {
				Spell spell = MagicSpells.getSpellByInternalName(spellName);
				if (spell != null && spell instanceof TargetedLocationSpell) {
					spells.add((TargetedLocationSpell)spell);
				}
			}
		}
		if (spells.size() == 0) {
			MagicSpells.error("Pulse spell '" + internalName + "' has no spells defined!");
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(MagicSpells.getTransparentBlocks(), range);
			if (target == null || target.getType() == Material.AIR) {
				return noTarget(player);
			}
			target = target.getRelative(BlockFace.UP);
			if (target.getType() != Material.AIR && target.getType() != Material.SNOW && target.getType() != Material.LONG_GRASS) {
				return noTarget(player);
			}
			createPulser(player, target, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	private void createPulser(Player caster, Block block, float power) {
		block.setTypeIdAndData(typeId, data, true);
		pulsers.put(block, new Pulser(caster, block, power));
	}
	
	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		Block block = target.getBlock();
		if (block.getType() == Material.AIR || block.getType() == Material.SNOW || block.getType() == Material.LONG_GRASS) {
			createPulser(caster, block, power);
			return true;
		} else {
			block = block.getRelative(BlockFace.UP);
			if (block.getType() == Material.AIR || block.getType() == Material.SNOW || block.getType() == Material.LONG_GRASS) {
				createPulser(caster, block, power);
				return true;
			} else {
				return false;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onBlockBreak(BlockBreakEvent event) {
		Pulser pulser = pulsers.get(event.getBlock());
		if (pulser != null) {
			if (unbreakable) {
				event.setCancelled(true);
			} else {
				pulser.stop();
			}
		}
	}
	
	public class Pulser implements Runnable {
		
		Player caster;
		Block block;
		Location location;
		float power;
		int pulseCount;
		int taskId;
		
		public Pulser(Player caster, Block block, float power) {
			this.caster = caster;
			this.block = block;
			this.location = block.getLocation();
			this.power = power;
			this.pulseCount = 0;
			
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, 0, interval);
		}
		
		public void run() {
			if (!caster.isDead() && caster.isOnline()) {
				for (TargetedLocationSpell spell : spells) {
					spell.castAtLocation(caster, location, power);
				}
				if (totalPulses > 0) {
					pulseCount += 1;
					if (pulseCount >= totalPulses) {
						stop();
					}
				}
			} else {
				stop();
			}
		}
		
		public void stop() {
			Bukkit.getScheduler().cancelTask(taskId);
			pulsers.remove(block);
			block.setType(Material.AIR);
		}
		
	}

}
