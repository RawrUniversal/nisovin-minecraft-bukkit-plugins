package com.nisovin.magicspells.spells;

import java.util.HashSet;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import com.nisovin.magicspells.*;
import com.nisovin.magicspells.util.MagicConfig;

public class BlinkSpell extends InstantSpell {
	
	private boolean smokeTrail;
	private String strCantBlink = null;
	
	public BlinkSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		smokeTrail = config.getBoolean("spells." + spellName + ".smoke-trail", true);
		strCantBlink = config.getString("spells." + spellName + ".str-cant-blink", "You can't blink there.");
	}
	
	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			int range = Math.round(this.range*power);
			if (range <= 0) range = 25;
			if (range > 125) range = 125;
			BlockIterator iter = new BlockIterator(player, range>0&&range<150?range:150);
			HashSet<Location> smokes = null;
			if (smokeTrail) {
				smokes = new HashSet<Location>();
			}
			Block prev = null;
			Block found = null;
			Block b;
			while (iter.hasNext()) {
				b = iter.next();
				if (b.getType() == Material.AIR) {
					prev = b;
					if (smokeTrail) {
						smokes.add(b.getLocation());
					}
				} else {
					found = b;
					break;
				}
			}
			
			if (found != null) {
				Location loc = null;
				if (range > 0 && !inRange(found.getLocation(), player.getLocation(), range)) {
				} else if (found.getRelative(0,1,0).getType() == Material.AIR && found.getRelative(0,2,0).getType() == Material.AIR) {
					loc = found.getLocation();
					loc.setY(loc.getY() + 1);
				} else if (prev != null && prev.getType() == Material.AIR && prev.getRelative(0,1,0).getType() == Material.AIR) {
					loc = prev.getLocation();
				}
				if (loc != null) {
					loc.setX(loc.getX()+.5);
					loc.setZ(loc.getZ()+.5);
					loc.setPitch(player.getLocation().getPitch());
					loc.setYaw(player.getLocation().getYaw());
					player.teleport(loc);
					if (smokeTrail) {
						for (Location l : smokes) {
							l.getWorld().playEffect(l, Effect.SMOKE, 4);
						}
					}
				} else {
					sendMessage(player, strCantBlink);
					return PostCastAction.ALREADY_HANDLED;
				}
			} else {
				sendMessage(player, strCantBlink);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
