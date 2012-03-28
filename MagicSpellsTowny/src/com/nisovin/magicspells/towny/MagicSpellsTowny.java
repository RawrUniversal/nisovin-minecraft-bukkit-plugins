package com.nisovin.magicspells.towny;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.palmergames.bukkit.towny.NotRegisteredException;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyException;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.object.WorldCoord;

public class MagicSpellsTowny extends JavaPlugin implements Listener {

	private Towny towny;
	
	private Set<Spell> disallowedInTowns = new HashSet<Spell>();
	
	@Override
	public void onEnable() {
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			saveDefaultConfig();
		}
		Configuration config = getConfig();
		if (config.contains("disallowed-in-towns")) {
			List<String> list = config.getStringList("disallowed-in-towns");
			for (String s : list) {
				Spell spell = MagicSpells.getSpellByInternalName(s);
				if (spell == null) {
					spell = MagicSpells.getSpellByInGameName(s);
				}
				if (spell != null) {
					disallowedInTowns.add(spell);
				} else {
					getLogger().warning("Could not find spell " + s);
				}
			}
		}
		
		Plugin townyPlugin = getServer().getPluginManager().getPlugin("Towny");
		if (townyPlugin != null) {
			towny = (Towny)townyPlugin;
			getServer().getPluginManager().registerEvents(this, this);
			getLogger().info("All systems go");
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onSpellTarget(SpellTargetEvent event) {
		if (event.getTarget() instanceof Player) {
			try {
				TownyWorld world = TownyUniverse.getDataSource().getWorld(event.getCaster().getWorld().getName());
				if (preventDamageCall(world, event.getCaster(), (Player)event.getTarget())) {
					event.setCancelled(true);
				}
			} catch (NotRegisteredException e) {
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onSpellCast(SpellCastEvent event) {
		if (disallowedInTowns.contains(event.getSpell())) {
			try {
				TownyWorld world = TownyUniverse.getDataSource().getWorld(event.getCaster().getWorld().getName());
				if (world != null && world.isUsingTowny()) {
					Coord coord = Coord.parseCoord(event.getCaster());
					if (world.getTownBlock(coord) != null) {
						event.setCancelled(true);
					}
				}
			} catch (NotRegisteredException e) {
			}
		}
	}

	public boolean preventDamageCall(TownyWorld world, Player ap, Player bp) {
		// World using Towny
		if (!world.isUsingTowny())
			return false;

		Coord coord = Coord.parseCoord(bp);

		if (world.isWarZone(coord))
			return false;

		if (preventFriendlyFire(ap, bp))
			return true;

		try {
			// Check Town PvP status
			TownBlock townblock = world.getTownBlock(coord);
			if (!townblock.getTown().isPVP() && !world.isForcePVP() && !townblock.getPermissions().pvp) {
				return true;
			}
		} catch (NotRegisteredException e) {
			// Not in a town
			if ((!world.isPVP()) && (!world.isForcePVP()))
				return true;
		}

		return false;
	}

	public boolean preventFriendlyFire(Player a, Player b) {
		TownyUniverse universe = towny.getTownyUniverse();
		if (!TownySettings.getFriendlyFire() && universe.isAlly(a.getName(), b.getName())) {
			try {
				TownyWorld world = TownyUniverse.getDataSource().getWorld(b.getWorld().getName());
				TownBlock townBlock = new WorldCoord(world, Coord.parseCoord(b)).getTownBlock();
				if (!townBlock.getType().equals(TownBlockType.ARENA))
					return true;
			} catch (TownyException x) {
				return true;
			}
		}
		return false;
	}
	
}
