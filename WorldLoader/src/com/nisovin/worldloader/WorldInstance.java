package com.nisovin.worldloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class WorldInstance {
	
	private WorldBase base;
	private World world;
	private String worldName;
	private Location start;
	private HashMap<Player,Location> previousLocations = new HashMap<Player,Location>();
	private Location respawn;
	
	public WorldInstance(WorldBase base, boolean allowWorldGen) {
		this(base, allowWorldGen, false);
	}
	
	public WorldInstance(WorldBase base, String worldName) {
		this.base = base;
		this.worldName = worldName;
		loadSavedWorld();
	}
	
	public WorldInstance(WorldBase base, boolean allowWorldGen, boolean newWorld) {
		this.base = base;
		if (newWorld) {
			createNewWorld();
		} else {
			createAndLoadWorld(allowWorldGen);
		}
	}
	
	public String getWorldName() {
		return worldName;
	}
	
	public World getInstanceWorld() {
		return world;
	}
	
	public List<Player> getPlayers() {
		return world.getPlayers();
	}
	
	public Location getStartLocation() {
		return start;
	}
	
	public void teleport(Player player) {
		if (!player.getLocation().getWorld().equals(world)) {
			previousLocations.put(player, player.getLocation().clone());
		}
		if (respawn != null) {
			player.teleport(respawn);
		} else if (start != null) {
			player.teleport(start);
		} else {
			player.teleport(world.getHighestBlockAt(world.getSpawnLocation()).getLocation());
		}
	}
	
	public void setRespawn(Location location) {
		respawn = location;
	}
	
	public WorldBase getBase() {
		return base;
	}
	
	public Location getRespawn() {
		if (respawn != null) {
			return respawn;
		} else {
			return start;
		}
	}
	
	public boolean monstersEnabled() {
		return base.monstersEnabled();
	}
	
	public boolean canBreak(Block block) {
		return base.canBreak(block);
	}
	
	public boolean canPlace(Block block) {
		return base.canPlace(block);
	}
	
	public boolean worldLoaded() {
		return (world != null);
	}
	
	private void createAndLoadWorld(boolean allowWorldGen) {
		// get source folder
		File src = base.getWorldFolder();
		if (!src.exists() || !src.isDirectory()) {
			throw new IllegalStateException("No such base world found: " + src.getAbsolutePath());
		}
		
		// get destination folder
		worldName = base.getName() + "-" + System.currentTimeMillis();
		File dest = new File(Bukkit.getWorldContainer(), worldName);
		if (dest.exists()) {
			throw new IllegalStateException("Instance world already exists! " + base.getName());
		}
		
		// copy world
		try {
			copyFolder(src, dest);
		} catch (IOException e) {
			System.out.println("Failed to copy world: " + base.getName());
			e.printStackTrace();
		}
		
		// load world
		WorldCreator wc = WorldCreator.name(worldName);
		wc.environment(Environment.NORMAL);
		if (!allowWorldGen) {
			wc.generator(new EmptyWorldGen());
		}
		//wc.seed(base.getSeed());
		world = wc.createWorld();
		
		// get start
		String s = base.getStartLocationString();
		if (!s.isEmpty()) {
			try {
				String[] coords = s.split(",");
				start = new Location(world, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]), Float.parseFloat(coords[3]), Float.parseFloat(coords[4]));
			} catch (Exception e) {
				start = world.getSpawnLocation();
			}
		} else {
			start = world.getHighestBlockAt(world.getSpawnLocation()).getLocation();
		}
	}
	
	private void createNewWorld() {
		// create world
		worldName = base.getName() + "-" + System.currentTimeMillis();
		WorldCreator wc = WorldCreator.name(worldName);
		wc.environment(Environment.NORMAL);
		wc.seed(base.getSeed());
		world = wc.createWorld();
		
		// get start
		start = world.getHighestBlockAt(world.getSpawnLocation()).getLocation();
	}
	
	private void loadSavedWorld() {
		File folder = new File(Bukkit.getWorldContainer(), worldName);
		if (!folder.exists()) {
			return;
		}
		
		// load world
		WorldCreator wc = WorldCreator.name(worldName);
		wc.environment(Environment.NORMAL);
		wc.generator(new EmptyWorldGen());
		//wc.seed(base.getSeed());
		world = wc.createWorld();
		
		// get config
		File file = new File(folder, "data.yml");
		if (file.exists()) {
			YamlConfiguration config = new YamlConfiguration();
			try {
				config.load(file);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		
		// get start
		String s = base.getStartLocationString();
		if (!s.isEmpty()) {
			try {
				String[] coords = s.split(",");
				start = new Location(world, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
			} catch (Exception e) {
				start = world.getSpawnLocation();
			}
		} else {
			start = world.getHighestBlockAt(world.getSpawnLocation()).getLocation();
		}
	}
	
	public void eject(Player player) {
		Location loc = previousLocations.get(player);
		if (loc == null) {
			loc = Bukkit.getWorlds().get(0).getSpawnLocation();
		}
		player.teleport(loc);
	}
	
	public void unloadWorld() {
		unloadWorld(false);
	}
	
	public void unloadWorld(boolean delete) {
		// teleport players away
		for (Player p : world.getPlayers()) {
			eject(p);
		}
		
		// unload world
		Bukkit.getServer().unloadWorld(world, !delete);
		world = null;
		
		// delete world folder
		if (delete) {
			deleteFolder(new File(worldName));
		}
	}
	
	public void overwriteBase() {
		// delete old base
		File oldBase = base.getWorldFolder();
		File backup = new File(WorldLoader.plugin.getDataFolder(), "worlds" + File.separator + oldBase.getName() + "-backup-" + System.currentTimeMillis());
		//deleteFolder(oldBase);
		oldBase.renameTo(backup);
		
		// get current file
		File newBase = new File(Bukkit.getWorldContainer(), worldName);
		
		// copy to base
		try {
			copyFolder(newBase, oldBase);
		} catch (IOException e) {
			System.out.println("Failed to overwrite base: " + base.getName());
			e.printStackTrace();
		}
		
		// delete uid file
		File uid = new File(base.getWorldFolder(), "uid.dat");
		if (uid.exists()) {
			uid.delete();
		}
	}
	
	private void copyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
            if (!dest.exists()) {
            	dest.mkdir();
            } 
            String[] oChildren = src.list();
            for (int i=0; i < oChildren.length; i++) 
            {
                copyFolder(new File(src, oChildren[i]), new File(dest, oChildren[i]));
            }
        } else {
            copyFile(src, dest);
        }
	}
	
	private void copyFile(File srcFile, File destFile) throws IOException {
        InputStream oInStream = new FileInputStream(srcFile);
        OutputStream oOutStream = new FileOutputStream(destFile);
 
        byte[] oBytes = new byte[1024];
        int nLength;
        BufferedInputStream oBuffInputStream = new BufferedInputStream(oInStream);
        while ((nLength = oBuffInputStream.read(oBytes)) > 0) {
        	oOutStream.write(oBytes, 0, nLength);
        }
        oInStream.close();
        oOutStream.close();
	}
	
	private void deleteFolder(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				deleteFolder(f);
			}
		}
		file.delete();
	}
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof WorldInstance && ((WorldInstance)o).worldName.equals(this.worldName));
	}
	
	@Override
	public int hashCode() {
		return worldName.hashCode();
	}
	
}
