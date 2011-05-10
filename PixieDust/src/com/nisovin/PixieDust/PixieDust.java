package com.nisovin.PixieDust;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;


public class PixieDust extends JavaPlugin {

	public static int FLY_ITEM = 0;
	public static boolean REQUIRE_ITEM_WHILE_FLYING = false;
	public static int ACTIVATION_PITCH = 75;
	public static int TICK_DELAY = 0;
	public static int TICK_INTERVAL = 15;
	public static int FLY_SPEED = 20;
	public static int Y_OFFSET = 6;
	public static String FLY_START = "You are now flying!";
	public static String FLY_STOP = "You are no longer flying.";
	
	@Override
	public void onEnable() {

		loadConfig();
		
		PDPlayerListener playerListener = new PDPlayerListener(this);
		PDEntityListener entityListener = new PDEntityListener(this);

		this.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, Priority.Normal, this);
		this.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
		
		
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}
	
	public void loadConfig() {
		File dataFolder = this.getDataFolder();
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}
		
		File file = new File(dataFolder, "config.txt");
		if (!file.exists()) {
			// no config - create new config file
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));
				writer.write("settings:");
				writer.newLine();
				writer.write("    fly-item: " + FLY_ITEM);
				writer.newLine();
				writer.write("    require-item-while-flying: " + (REQUIRE_ITEM_WHILE_FLYING?"true":"false"));
				writer.newLine();
				writer.write("    tick-delay: " + TICK_DELAY);
				writer.newLine();
				writer.write("    tick-interval: " + TICK_INTERVAL);
				writer.newLine();
				writer.write("    fly-speed: " + FLY_SPEED);
				writer.newLine();
				writer.write("    fly-start: " + FLY_START);
				writer.newLine();
				writer.write("    fly-stop: " + FLY_STOP);
				writer.newLine();
				writer.close();
			} catch (IOException e) {
			}
		} else {
			Configuration config = new Configuration(file);
			FLY_ITEM = config.getInt("settings.fly-item", FLY_ITEM);
			REQUIRE_ITEM_WHILE_FLYING = config.getBoolean("settings.require-item-while-flying", REQUIRE_ITEM_WHILE_FLYING);
			TICK_DELAY = config.getInt("settings.tick-delay", TICK_DELAY);
			TICK_INTERVAL = config.getInt("settings.tick-interval", TICK_INTERVAL);
			FLY_SPEED = config.getInt("settings.fly-speed", FLY_SPEED);
			FLY_START = config.getString("settings.fly-start", FLY_START);
			FLY_STOP = config.getString("settings.fly-stop", FLY_STOP);			
		}
	}


}
