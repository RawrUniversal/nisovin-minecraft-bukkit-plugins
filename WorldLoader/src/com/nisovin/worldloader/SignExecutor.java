package com.nisovin.worldloader;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class SignExecutor {
	
	public static void executeSign(Block block, Player player) {
		System.out.println("Executing " + block);
		
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
			return;
		}
		
		Sign sign = (Sign)block.getState();
		String[] lines = sign.getLines();
		if (!lines[0].equals("[ACTION]")) {
			return;
		}
		
		for (int i = 1; i < 4; i++) {
			String line = lines[i];
			if (line != null && !line.isEmpty()) {
				System.out.println(line);
				executeLine(line, block, player);
			}
		}
	}
	
	private static void executeLine(String line, Block block, Player player) {
		if (line.matches("^SET -?[0-9]*,-?[0-9]*,-?[0-9]* [0-9]+(:[0-9]+)?$")) {
			set(line, block);
		} else if (line.matches("^TP -?[0-9]+,[0-9]+,-?[0-9]+$") && player != null) {
			tp(line, player);
		} else if (line.matches("^TPA -?[0-9]+,[0-9]+,-?[0-9]+$")) {
			tpAll(line, block);
		} else if (line.matches("^CP -?[0-9]+,[0-9]+,-?[0-9]+$")) {
			checkpoint(line, block);
		} else if (line.matches("^MOB (Cr|Sk|Sp|Gi|Zo|Sl|Gh|ZP|En|CS|Si|Bl|Bl,MC,ED,SG,Wo) -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			spawnMob(line, block);
		} else if (line.matches("^EXE -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			exe(line, block, player);
		} else if (line.matches("^EXE -?[0-9]*,-?[0-9]*,-?[0-9]* [0-9]+(T|S|M)?$")) {
			exeDelayed(line, block, player);
		} else if (line.matches("^SAY -?[0-9]*,-?[0-9]*,-?[0-9]*$") && player != null) {
			say(line, block, player);
		} else if (line.matches("^SAYA -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			sayAll(line, block);
		} else if (line.matches("^CLI -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			consoleCommand(line, block, player);
		} else if (line.matches("^WEA (rain|sun)$")) {
			weather(line, block);
		} else if (line.matches("^TIME [0-9]+$")) {
			time(line, block);
		}
	}
	
	private static void set(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		int type, data;
		if (args[2].contains(":")) {
			String[] typedata = args[2].split(":");
			type = Integer.parseInt(typedata[0]);
			data = Integer.parseInt(typedata[1]);
		} else {
			type = Integer.parseInt(args[2]);
			data = 0;
		}
		
		block.getRelative(x,y,z).setTypeIdAndData(type, (byte)data, true);
	}
	
	private static void tp(String line, Player player) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",");
		
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		
		player.teleport(new Location(player.getWorld(), x, y, z), TeleportCause.PLUGIN);
	}
	
	private static void tpAll(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",");
		
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		
		Location loc = new Location(block.getWorld(), x, y, z);
		for (Player player : block.getWorld().getPlayers()) {
			player.teleport(loc, TeleportCause.PLUGIN);
		}
	}
	
	private static void checkpoint(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",");
		
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		
		WorldInstance instance = WorldLoader.plugin.getWorldInstance(block.getWorld());
		if (instance != null) {
			Location loc = new Location(block.getWorld(), x, y, z);
			instance.setRespawn(loc);
		}
	}
	
	private static void spawnMob(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[2].split(",", 3);
		
		String c = args[1];
		CreatureType creature = null;
		if (c.equals("Cr")) {
			creature = CreatureType.CREEPER;
		} else if (c.equals("Sk")) {
			creature = CreatureType.SKELETON;
		} else if (c.equals("Sp")) {
			creature = CreatureType.SPIDER;
		} else if (c.equals("Zo")) {
			creature = CreatureType.ZOMBIE;
		}
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		Location location = new Location(block.getWorld(), block.getX() + x + .5, block.getY() + y + .2, block.getZ() + z + .5);
		if (creature != null) {
			block.getWorld().spawnCreature(location, creature);
		}
	}
	
	private static void exe(String line, Block block, Player player) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		Block sign = block.getRelative(x,y,z);
		executeSign(sign, player);
	}
	
	private static void exeDelayed(String line, Block block, final Player player) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		final Block sign = block.getWorld().getBlockAt(new Location(block.getWorld(), block.getX() + x, block.getY() + y, block.getZ() + z));
		Bukkit.getScheduler().scheduleSyncDelayedTask(WorldLoader.plugin, new Runnable() {
			public void run() {
				executeSign(sign, player);
			}
		}, Integer.parseInt(args[2]));
	}
	
	private static void say(String line, Block block, Player player) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		Block b = block.getRelative(x,y,z);
		if (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST) {
			return;
		}
		
		Sign sign = (Sign)b.getState();
		String[] lines = sign.getLines();
		String text = "";
		for (int i = 0; i < 4; i++) {
			text += lines[i] + " ";
		}
		text = text.trim();
		
		player.sendMessage(text);
	}
	
	private static void sayAll(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);

		Block b = block.getRelative(x,y,z);
		if (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST) {
			return;
		}
		
		Sign sign = (Sign)b.getState();
		String[] lines = sign.getLines();
		String text = "";
		for (int i = 0; i < 4; i++) {
			text += lines[i] + " ";
		}
		text = text.trim();
		
		for (Player p : block.getWorld().getPlayers()) {
			p.sendMessage(text);
		}
	}
	
	private static void consoleCommand(String line, Block block, Player player) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		Block b = block.getRelative(x,y,z);
		if (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST) {
			return;
		}
		
		Sign sign = (Sign)b.getState();
		String[] lines = sign.getLines();
		String text = "";
		for (int i = 0; i < 4; i++) {
			text += lines[i] + " ";
		}
		text = text.trim();
		
		if (player != null) {
			text = text.replace("%player%", player.getName());
		}
		
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
	}
	
	private static void weather(String line, Block block) {
		String[] args = line.split(" ");
		
		if (args[1].equals("rain")) {
			block.getWorld().setStorm(true);
		} else if (args[1].equals("sun")) {
			block.getWorld().setStorm(false);
		}
	}
	
	private static void time(String line, Block block) {
		String[] args = line.split(" ");
		
		int time = Integer.parseInt(args[1]);
		block.getWorld().setTime(time);
	}
	
}
