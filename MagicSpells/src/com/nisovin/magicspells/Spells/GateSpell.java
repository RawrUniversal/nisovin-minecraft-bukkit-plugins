package com.nisovin.MagicSpells.Spells;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.InstantSpell;
import com.nisovin.MagicSpells.MagicSpells;

public class GateSpell extends InstantSpell {
	
	private String world;
	private String coords;
	private boolean useSpellEffect;
	private String strGateFailed;

	public GateSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		world = config.getString("spells." + spellName + ".world", "CURRENT");
		coords = config.getString("spells." + spellName + ".coordinates", "SPAWN");
		useSpellEffect = config.getBoolean("spells." + spellName + ".use-spell-effect", true);
		strGateFailed = config.getString("spells." + spellName + ".str-gate-failed", "Unable to teleport.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get world
			World world;
			if (this.world.equals("CURRENT")) {
				world = player.getWorld();
			} else if (this.world.equals("DEFAULT")) {
				world = Bukkit.getServer().getWorlds().get(0);
			} else {
				world = Bukkit.getServer().getWorld(this.world);
			}
			if (world == null) {
				// fail -- no world
				Bukkit.getServer().getLogger().warning("MagicSpells: " + name + ": world " + this.world + " does not exist");
				sendMessage(player, strGateFailed);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get location
			Location location;
			if (coords.matches("^-?[0-9]+,[0-9]+,-?[0-9]+$")) {
				String[] c = coords.split(",");
				int x = Integer.parseInt(c[0]);
				int y = Integer.parseInt(c[1]);
				int z = Integer.parseInt(c[2]);
				location = new Location(world, x, y, z);
			} else if (coords.equals("SPAWN")) {
				location = world.getSpawnLocation();
			} else {
				// fail -- no location
				Bukkit.getServer().getLogger().warning("MagicSpells: " + name + ": " + this.coords + " is not a valid location");
				sendMessage(player, strGateFailed);
				return PostCastAction.ALREADY_HANDLED;
			}
			location.setX(location.getX()+.5);
			location.setZ(location.getZ()+.5);
			
			// check for landing point
			Block b = location.getBlock();
			if (b.getType() != Material.AIR || b.getRelative(0,1,0).getType() != Material.AIR) {
				// fail -- blocked
				Bukkit.getServer().getLogger().warning("MagicSpells: " + name + ": landing spot blocked");
				sendMessage(player, strGateFailed);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// spell effect
			if (useSpellEffect) {
				final HashSet<Block> portals = new HashSet<Block>();
				portals.add(b);
				portals.add(b.getRelative(0,1,0));
				b = player.getLocation().getBlock();
				if (b.getType() == Material.AIR) {
					portals.add(b);
				}
				if (b.getRelative(0,1,0).getType() == Material.AIR) {
					portals.add(b.getRelative(0,1,0));
				}
				for (Block block : portals) {
					block.setType(Material.PORTAL);
				}
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
					public void run() {
						for (Block block : portals) {
							if (block.getType() == Material.PORTAL) { 
								block.setType(Material.AIR);
							}
						}
					}					
				}, 10);
			}
			
			// teleport caster
			player.teleport(location);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
