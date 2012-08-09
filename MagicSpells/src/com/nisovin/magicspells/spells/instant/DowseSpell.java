package com.nisovin.magicspells.spells.instant;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class DowseSpell extends InstantSpell {

	private int typeId;
	private int radius;
	private boolean rotatePlayer;
	private boolean setCompass;
	
	public DowseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			
			Block foundBlock = null;
			
			Location loc = player.getLocation();
			World world = player.getWorld();
			int cx = loc.getBlockX();
			int cy = loc.getBlockY();
			int cz = loc.getBlockZ();
			for (int r = 1; r <= radius; r++) {
				for (int x = -radius; x <= radius; x++) {
					for (int y = -radius; y <= radius; y++) {
						for (int z = -radius; z <= radius; z++) {
							if (x == radius || y == radius || z == radius || -x == radius || -y == radius || -z == radius) {
								Block block = world.getBlockAt(cx + x, cy + y, cz + z);
								if (block.getTypeId() == typeId) {
									foundBlock = block;
									break;
								}
							}
						}
					}
				}
			}
			
			if (foundBlock != null) {
				if (rotatePlayer) {
					//player.getLocation().
					//foundBlock.getLocation().subtract(player.getLocation()).toVector();
				}
				if (setCompass) {
					player.setCompassTarget(foundBlock.getLocation());
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
