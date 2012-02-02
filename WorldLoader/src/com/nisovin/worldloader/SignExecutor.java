package com.nisovin.worldloader;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class SignExecutor {
	
	public static void executeSign(Block block, Player player) {
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
			return;
		}
		
		Sign sign = (Sign)block.getState();
		String[] lines = sign.getLines();
		if (!lines[0].equals("[SCRIPT]")) {
			return;
		}
		
		for (int i = 1; i < 4; i++) {
			String line = lines[i];
			if (line != null && !line.isEmpty()) {
				if (i < 3 && lines[i+1] != null && lines[i+1].startsWith("... ")) {
					line += " " + lines[i+1].substring(4);
					i++;
				}
				executeLine(line, block, player);
			}
		}
	}
	
	private static void executeLine(String line, Block block, Player player) {
		if (line.matches("^S(ET|T)? -?[0-9]*,-?[0-9]*,-?[0-9]* [0-9]+(:[0-9]+)?$")) {
			set(line, block);
		} else if (line.matches("^TP? -?[0-9]+,[0-9]+,-?[0-9]+$") && player != null) {
			tp(line, player);
		} else if (line.matches("^T(A|PA) -?[0-9]+,[0-9]+,-?[0-9]+$")) {
			tpAll(line, block);
		} else if (line.matches("^T(R|PR) -?[0-9]*,-?[0-9]*,-?[0-9]*$") && player != null) {
			tpRelative(line, block, player);
		} else if (line.matches("^CP -?[0-9]+,[0-9]+,-?[0-9]+$")) {
			checkpoint(line, block);
		} else if (line.matches("^GLD -?[0-9]*,-?[0-9]*,-?[0-9]* [0-9]+")) {
			dropGold(line, block);
		} else if (line.matches("^EXP [0-9]+$") && player != null) {
			giveExp(line, player);
		} else if (line.matches("^EXPA [0-9]+$")) {
			giveExpAll(line, block);
		} else if (line.matches("^MOB (Bl|Ca|Ch|Co|Cr|En|Gh|Gi|Ma|Mo|Pi|Sh|Si|Sk|Sl|Sn|Sp|Sq|Wo|Zo|ZP) -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			spawnMob(line, block);
		} else if (line.matches("^EXE -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			exe(line, block, player);
		} else if (line.matches("^EXED -?[0-9]*,-?[0-9]*,-?[0-9]* [0-9]+(T|S|M)?$")) {
			exeDelayed(line, block, player);
		} else if (line.matches("^SAY -?[0-9]*,-?[0-9]*,-?[0-9]*$") && player != null) {
			say(line, block, player);
		} else if (line.matches("^SAYA -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			sayAll(line, block);
		} else if (line.matches("^EFF -?[0-9]*,-?[0-9]*,-?[0-9]* (Blz|Bow|C1|C2|Dr|End|Ext|Gh1|Gh2|Mob|Po|Rc|Sm|Bk)( [0-9]+)?$")) {
			playEffect(line, block);
		} else if (line.matches("^CLI -?[0-9]*,-?[0-9]*,-?[0-9]*$")) {
			consoleCommand(line, block, player);
		} else if (line.matches("^WEA (rain|sun)$")) {
			weather(line, block);
		} else if (line.matches("^TIME [0-9]+$")) {
			time(line, block);
		} else if (line.equals("DISABLE")) {
			disable(line, block);
		} else if (line.equals("END")) {
			end(line, block);
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
		
		player.teleport(new Location(player.getWorld(), x+.5, y+.5, z+.5), TeleportCause.PLUGIN);
	}
	
	private static void tpAll(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",");
		
		int x = Integer.parseInt(coords[0]);
		int y = Integer.parseInt(coords[1]);
		int z = Integer.parseInt(coords[2]);
		
		Location loc = new Location(block.getWorld(), x+.5, y+.5, z+.5);
		for (Player player : block.getWorld().getPlayers()) {
			player.teleport(loc, TeleportCause.PLUGIN);
		}
	}
	
	private static void tpRelative(String line, Block block, Player player) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		Location loc = block.getRelative(x, y, z).getLocation().add(.5, .5, .5);
		player.teleport(loc, TeleportCause.PLUGIN);		
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
	
	private static void dropGold(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		int amt = Integer.parseInt(args[2]);
		
		Location loc = block.getRelative(x, y, z).getLocation().add(.5, .5, .5);
		WorldLoader.plugin.dropGold(loc, amt);
	}
	
	private static void giveExp(String line, Player player) {
		String[] args = line.split(" ");
		int amt = Integer.parseInt(args[1]);
		
		player.giveExp(amt);
	}
	
	private static void giveExpAll(String line, Block block) {
		String[] args = line.split(" ");
		int amt = Integer.parseInt(args[1]);
		
		for (Player player : block.getWorld().getPlayers()) {
			player.giveExp(amt);
		}
	}
	
	private static void spawnMob(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[2].split(",", 3);
		
		String c = args[1];
		CreatureType creature = null;
		if (c.equals("Bl")) {
			creature = CreatureType.BLAZE;
		} else if (c.equals("Ca")) {
			creature = CreatureType.CAVE_SPIDER;
		} else if (c.equals("Ch")) {
			creature = CreatureType.CHICKEN;
		} else if (c.equals("Co")) {
			creature = CreatureType.COW;
		} else if (c.equals("Cr")) {
			creature = CreatureType.CREEPER;
		} else if (c.equals("En")) {
			creature = CreatureType.ENDERMAN;
		} else if (c.equals("Gh")) {
			creature = CreatureType.GHAST;
		} else if (c.equals("Gi")) {
			creature = CreatureType.GIANT;
		} else if (c.equals("Ma")) {
			creature = CreatureType.MAGMA_CUBE;
		} else if (c.equals("Mo")) {
			creature = CreatureType.MUSHROOM_COW;
		} else if (c.equals("Pi")) {
			creature = CreatureType.PIG;
		} else if (c.equals("Sh")) {
			creature = CreatureType.SHEEP;
		} else if (c.equals("Si")) {
			creature = CreatureType.SILVERFISH;
		} else if (c.equals("Sk")) {
			creature = CreatureType.SKELETON;
		} else if (c.equals("Sl")) {
			creature = CreatureType.SLIME;
		} else if (c.equals("Sn")) {
			creature = CreatureType.SNOWMAN;
		} else if (c.equals("Sp")) {
			creature = CreatureType.SPIDER;
		} else if (c.equals("Sq")) {
			creature = CreatureType.SQUID;
		} else if (c.equals("Wo")) {
			creature = CreatureType.WOLF;
		} else if (c.equals("Zo")) {
			creature = CreatureType.ZOMBIE;
		} else if (c.equals("ZP")) {
			creature = CreatureType.PIG_ZOMBIE;
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
				if (sign.getWorld() != null) {
					System.out.println("executing delayed...");
					executeSign(sign, player);					
				}
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
	
	private static void playEffect(String line, Block block) {
		String[] args = line.split(" ");
		String[] coords = args[1].split(",", 3);
		
		int x = coords[0].isEmpty() ? 0 : Integer.parseInt(coords[0]);
		int y = coords[1].isEmpty() ? 0 : Integer.parseInt(coords[1]);
		int z = coords[2].isEmpty() ? 0 : Integer.parseInt(coords[2]);
		
		Location loc = block.getRelative(x, y, z).getLocation();
		
		Effect effect = null;
		if (args[2].equals("Blz")) {
			effect = Effect.BLAZE_SHOOT;
		} else if (args[2].equals("Bow")) {
			effect = Effect.BOW_FIRE;
		} else if (args[2].equals("C1")) {
			effect = Effect.CLICK1;
		} else if (args[2].equals("C2")) {
			effect = Effect.CLICK2;
		} else if (args[2].equals("Dr")) {
			effect = Effect.DOOR_TOGGLE;
		} else if (args[2].equals("End")) {
			effect = Effect.ENDER_SIGNAL;
		} else if (args[2].equals("Ext")) {
			effect = Effect.EXTINGUISH;
		} else if (args[2].equals("Gh1")) {
			effect = Effect.GHAST_SHOOT;
		} else if (args[2].equals("Gh2")) {
			effect = Effect.GHAST_SHRIEK;
		} else if (args[2].equals("Mob")) {
			effect = Effect.MOBSPAWNER_FLAMES;
		} else if (args[2].equals("Po")) {
			effect = Effect.POTION_BREAK;
		} else if (args[2].equals("Rc")) {
			effect = Effect.RECORD_PLAY;
		} else if (args[2].equals("Sm")) {
			effect = Effect.SMOKE;
		} else if (args[2].equals("Bk")) {
			effect = Effect.STEP_SOUND;
		}
		
		int data = 0;
		if (args.length > 3) {
			data = Integer.parseInt(args[3]);
		}
		
		System.out.println(effect + " " + data);
		
		block.getWorld().playEffect(loc, effect, data);
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
			text = text.replace("%p%", player.getName());
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
	
	private static void disable(String line, Block block) {
		Sign sign = (Sign)block.getState();
		sign.setLine(0, "");
		sign.update();
	}
	
	private static void end(String line, Block block) {
		WorldInstance instance = WorldLoader.plugin.getWorldInstance(block.getWorld());
		if (instance != null) {
			WorldLoader.plugin.killInstance(instance, true);
		}
	}

	
}
