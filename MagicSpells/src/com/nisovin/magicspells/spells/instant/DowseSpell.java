package com.nisovin.magicspells.spells.instant;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.ItemNameResolver.ItemTypeAndData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.Util;

public class DowseSpell extends InstantSpell {

	private int typeId;
	private byte data;
	private int radius;
	private boolean rotatePlayer;
	private boolean setCompass;
	private String strNotFound;
	
	public DowseSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		ItemTypeAndData typeAndData = MagicSpells.getItemNameResolver().resolve(getConfigString("type", "iron_ore"));
		typeId = typeAndData.id;
		data = (byte)typeAndData.data;
		
		radius = getConfigInt("radius", 4);
		rotatePlayer = getConfigBoolean("rotate-player", true);
		setCompass = getConfigBoolean("set-compass", true);
		strNotFound = getConfigString("str-not-found", "No dowsing target found.");
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
				for (int x = -r; x <= r; x++) {
					for (int y = -r; y <= r; y++) {
						for (int z = -r; z <= r; z++) {
							if (x == r || y == r || z == r || -x == r || -y == r || -z == r) {
								Block block = world.getBlockAt(cx + x, cy + y, cz + z);
								if (block.getTypeId() == typeId && block.getData() == data) {
									foundBlock = block;
									break;
								}
							}
						}
						if (foundBlock != null) break;
					}
					if (foundBlock != null) break;
				}
				if (foundBlock != null) break;
			}
						
			if (foundBlock == null) {
				sendMessage(player, strNotFound);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				if (rotatePlayer) {
					Vector v = foundBlock.getLocation().add(.5, .5, .5).subtract(player.getEyeLocation()).toVector().normalize();
					Util.setFacing(player, v);
				}
				if (setCompass) {
					player.setCompassTarget(foundBlock.getLocation());
				}
				
				playSpellEffects(EffectPosition.CASTER, player);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
