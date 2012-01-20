package com.nisovin.worldloader;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.nisovin.worldloader.PendingAction.ActionType;

public class CommandExec implements CommandExecutor {

	WorldLoader plugin;
	
	public CommandExec(WorldLoader plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		String comm = command.getName();
		if (comm.equals("loadworld")) {
			return loadWorld(sender, args);
		} else if (comm.equals("saveworld") && sender instanceof Player) {
			return saveWorld(sender, args);
		} else if (comm.equals("newworld")) {
			return newWorld(sender, args);
		} else if (comm.equals("start")) {
			return start(sender, args);
		} else if (comm.equals("minplayers")) {
			return setMin(sender, args);
		} else if (comm.equals("maxplayers")) {
			return setMax(sender, args);
		} else if (comm.equals("breakable")) {
			return setBreakables(sender, args);
		} else if (comm.equals("placeable")) {
			return setPlaceables(sender, args);
		} else if (comm.equals("monsters")) {
			return setMonsters(sender, args);
		} else if (comm.equals("party")) {
			return party(sender, args);
		} else if (comm.equals("invite")) {
			return invite(sender, args);
		} else if (comm.equals("leave")) {
			return leave(sender, args);
		}
		return true;
	}
	
	// *** ADMIN WORLD COMMANDS *** //
	
	private boolean loadWorld(CommandSender sender, String[] args) {
		if (args != null && args.length == 1) {
			WorldBase base = plugin.worldBases.get(args[0]);
			if (base != null) {
				WorldInstance instance = plugin.launchInstance(base, true);
				if (instance != null && sender instanceof Player) {
					Player player = (Player)sender;
					plugin.playerLocations.put(player.getName().toLowerCase(), instance.getWorldName());
					instance.teleport(player);
				}
			}
		}
		return true;
	}
	
	private boolean saveWorld(CommandSender sender, String[] args) {
		final Player player = (Player)sender;
		final WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You must be in an instance to do this.");
		} else {
			instance.getInstanceWorld().save();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					instance.overwriteBase();
					player.sendMessage("World base saved.");
				}
			}, 40);
		}
		return true;
	}
	
	private boolean newWorld(CommandSender sender, String[] args) {
		if (args.length < 3) {
			return false;
		}
		
		String worldName = args[0];
		if (!worldName.matches("^[A-Za-z0-9_]+$")) {
			return false;
		}
		
		String seed = args[1];
		if (!seed.matches("[0-9]+")) {
			return false;
		}
		
		String worldDesc = args[2];
		for (int i = 3; i < args.length; i++) {
			worldDesc += " " + args[i];
		}
		
		sender.sendMessage("Creating new world base...");
		
		Configuration config = plugin.getConfig();
		ConfigurationSection section = config.createSection("worlds." + worldName);
		section.set("folder", worldName);
		section.set("description", worldDesc);
		section.set("seed", Long.parseLong(seed));
		plugin.saveConfig();
		
		WorldBase base = new WorldBase(worldName, section);
		plugin.worldBases.put(worldName, base);
		
		WorldInstance instance = new WorldInstance(base, true, true);
		plugin.loadedWorlds.put(instance.getWorldName(), instance);
		if (sender instanceof Player) {
			instance.teleport((Player)sender);
		}
		
		sender.sendMessage("New world base created!");
		return true;
	}

	private boolean start(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You are not in an instance.");
			return true;
		}
		
		Location loc = player.getLocation();
		String location = loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
		instance.getBase().setStartLocationString(location);
		setConfigValue(instance, "start", location);
		return true;
	}
	
	private boolean setMin(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		if (args.length != 1 || !args[0].matches("^[0-9]+$")) {
			return false;
		}
		
		int min = Integer.parseInt(args[0]);
		
		WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You are not in an instance.");
			return true;
		}
		
		instance.getBase().setMinPlayers(min);
		setConfigValue(instance, "min-players", min);
		
		return true;
	}
	
	private boolean setMax(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		if (args.length != 1 || !args[0].matches("^[0-9]+$")) {
			return false;
		}
		
		int min = Integer.parseInt(args[0]);
		
		WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You are not in an instance.");
			return true;
		}
		
		instance.getBase().setMaxPlayers(min);
		setConfigValue(instance, "max-players", min);
		
		return true;
	}
	
	private boolean setBreakables(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		if (args.length != 1 || !args[0].matches("^[0-9]+(,[0-9]+)*$")) {
			return false;
		}
		
		String[] s = args[0].split(",");
		int[] types = new int[s.length];
		for (int i = 0; i < s.length; i++) {
			types[i] = Integer.parseInt(s[i]);
		}
		
		WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You are not in an instance.");
			return true;
		}
		
		instance.getBase().setBreakable(types);
		setConfigValue(instance, "breakable", types);
		
		return true;
	}
	
	private boolean setPlaceables(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		if (args.length != 1 || !args[0].matches("^[0-9]+(,[0-9]+)*$")) {
			return false;
		}
		
		String[] s = args[0].split(",");
		int[] types = new int[s.length];
		for (int i = 0; i < s.length; i++) {
			types[i] = Integer.parseInt(s[i]);
		}
		
		WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You are not in an instance.");
			return true;
		}
		
		instance.getBase().setPlaceable(types);
		setConfigValue(instance, "placeable", types);
		
		return true;
	}
	
	private boolean setMonsters(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		if (args.length != 1) {
			return false;
		}
		
		boolean val = false;
		if (args[0].equalsIgnoreCase("on")) {
			val = true;
		} else if (args[0].equalsIgnoreCase("off")) {
			val = false;
		} else {
			return false;
		}
		
		WorldInstance instance = plugin.getWorldInstance(player);
		if (instance == null) {
			player.sendMessage("You are not in an instance.");
			return true;
		}
		
		instance.getBase().setMonstersEnabled(val);
		setConfigValue(instance, "monsters", val);
		
		return true;
	}
	
	private void setConfigValue(WorldInstance instance, String key, Object value) {
		ConfigurationSection section = plugin.getConfig().getConfigurationSection("worlds." + instance.getBase().getName());
		section.set(key, value);
		plugin.saveConfig();
	}
	
	// *** PARTY COMMANDS *** //
	
	private boolean party(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		Party party = plugin.parties.get(player.getName().toLowerCase());
		if (party == null) {
			player.sendMessage("You are not in a party.");
		} else {
			String list = "";
			Set<String> members = party.getMembers();
			for (String s : members) {
				Player p = Bukkit.getPlayerExact(s);
				if (p != null) {
					if (list.isEmpty()) {
						list += p.getDisplayName();
					} else {
						list += ", " + p.getDisplayName();
					}
				}
			}
			player.sendMessage("Party members: " + list);
		}
		
		return true;
	}
	
	private boolean invite(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;
		
		if (args.length != 1) {
			return false;
		}
		
		// check if leader
		Party party = plugin.parties.get(player.getName().toLowerCase());
		if (party == null) {
			party = new Party(player);
		}
		if (!party.isLeader(player)) {
			player.sendMessage("You can only invite someone if you are the party leader.");
			return true;
		}
		
		// get target player
		Player target = null;
		List<Player> matches = Bukkit.matchPlayer(args[0]);
		if (matches.size() == 1) {
			target = matches.get(0);
		}
		if (target == null) {
			player.sendMessage("No such player found.");
			return true;
		}
		
		// check if target is already in a party
		if (plugin.parties.containsKey(target.getName().toLowerCase())) {
			player.sendMessage("That player is already in a party.");
			return true;
		}
		
		// check if target is in an instance
		if (!target.getWorld().equals(Bukkit.getWorlds().get(0))) {
			player.sendMessage("That player is in an instance.");
			return true;
		}
		
		// invite to party
		plugin.parties.put(player.getName().toLowerCase(), party);
		target.sendMessage(player.getDisplayName() + " has invited you to join a party.");
		target.sendMessage("Type 'yes' to accept this invitation.");
		PendingAction action = new PendingAction(target, ActionType.JOIN_PARTY, player.getName().toLowerCase());
		plugin.addPendingAction(action);
		player.sendMessage("Invitation sent.");
		return true;
	}
	
	private boolean leave(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return true;
		}
		Player player = (Player)sender;

		boolean warn = true;
		
		final WorldInstance instance = plugin.getWorldInstance(player);
		if (instance != null) {
			instance.eject(player);
			warn = false;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					if (instance.worldLoaded() && instance.getInstanceWorld().getPlayers().size() == 0) {
						plugin.killInstance(instance);
					}
				}
			}, 600);
		}
		
		Party party = plugin.parties.get(player.getName().toLowerCase());
		if (party == null) {
			if (warn) {
				player.sendMessage("You are not in a party.");
			}
		} else {
			party.remove(player);
			plugin.parties.remove(player.getName().toLowerCase());
		}
		
		return true;
	}
}
