package com.nisovin.MagicSpells.Spells;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.InstantSpell;

public class EntombSpell extends InstantSpell {

	private boolean targetPlayers;
	private boolean obeyLos;
	private int tombBlockType;
	private int tombDuration;
	private boolean closeTopAndBottom;
	private String strNoTarget;
	
	public EntombSpell(Configuration config, String spellName) {
		super(config, spellName);

		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		obeyLos = config.getBoolean("spells." + spellName + ".obey-los", true);
		tombBlockType = config.getInt("spells." + spellName + ".tomb-block-type", Material.GLASS.getId());
		tombDuration = config.getInt("spells." + spellName + ".tomb-duration", 20);
		closeTopAndBottom = config.getBoolean("spells." + spellName + ".close-top-and-bottom", true);
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			LivingEntity target = getTargetedEntity(player, range, targetPlayers, obeyLos);
			if (target != null) {
				int x = target.getLocation().getBlockX();
				int y = target.getLocation().getBlockY();
				int z = target.getLocation().getBlockZ();
				
				Location loc = new Location(target.getLocation().getWorld(), x+.5, y+.5, z+.5, target.getLocation().getYaw(), target.getLocation().getPitch());
				target.teleport(loc);
				
				ArrayList<Block> tombBlocks = new ArrayList<Block>();
				Block feet = target.getLocation().getBlock();
				
				Block temp = feet.getRelative(1,0,0);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(1,1,0);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(-1,0,0);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(-1,1,0);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(0,0,1);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(0,1,1);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(0,0,-1);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				temp = feet.getRelative(0,1,-1);
				if (temp.getType() == Material.AIR) {
					temp.setTypeId(tombBlockType);
					tombBlocks.add(temp);
				}
				if (closeTopAndBottom) {
					temp = feet.getRelative(0,-1,0);
					if (temp.getType() == Material.AIR) {
						temp.setTypeId(tombBlockType);
						tombBlocks.add(temp);
					}
					temp = feet.getRelative(0,2,0);
					if (temp.getType() == Material.AIR) {
						temp.setTypeId(tombBlockType);
						tombBlocks.add(temp);
					}
				}
				
				if (tombDuration > 0 && tombBlocks.size() > 0) {
					MagicSpells.plugin.getServer().getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new TombRemover(tombBlocks), tombDuration*20);
				}
			} else {
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			}
		}		
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private class TombRemover implements Runnable {

		ArrayList<Block> tomb;
		
		public TombRemover(ArrayList<Block> tomb) {
			this.tomb = tomb;
		}
		
		@Override
		public void run() {
			for (Block block : tomb) {
				if (block.getTypeId() == tombBlockType) {
					block.setType(Material.AIR);
				}
			}
		}
		
	}

}
