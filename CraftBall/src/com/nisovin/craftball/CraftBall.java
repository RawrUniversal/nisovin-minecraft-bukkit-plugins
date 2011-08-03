package com.nisovin.craftball;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class CraftBall extends JavaPlugin {
	
	protected HashSet<Field> fields = new HashSet<Field>();
	
	@Override
	public void onEnable() {
		new BallPlayerListener(this);
		new BallBlockListener(this);
		
		loadConfig();
		
		getServer().getLogger().info("CraftBall v" + this.getDescription().getVersion() + " enabled: " + fields.size() + " fields loaded.");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender.isOp() && command.getName().equals("craftballreload")) {
			fields.clear();
			loadConfig();
			sender.sendMessage("CraftBall config reloaded.");
		} else if (sender instanceof Player && sender.isOp() && command.getName().equals("craftballfield")) {
			Player player = (Player)sender;
			
		}
		return true;
	}
	
	public void loadConfig() {
		Configuration config = this.getConfiguration();
		config.load();		
		
		Map<String,ConfigurationNode> nodes = config.getNodes("fields");
		if (nodes != null) {
			for (ConfigurationNode node : nodes.values()) {
				Field field = new Field();
				
				// get region
				List<String> points = node.getStringList("region", null);
				if (points == null) {
					continue;
				}
				for (String p : points) {
					String[] point = p.split(",");
					field.region.addPoint(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
				}
				field.fieldY = node.getInt("region-y", 0);
				field.fieldHeight = node.getInt("field-height", 4);
				
				// ball item
				String ballItem = node.getString("ball-item");
				if (ballItem == null || ballItem.equals("")) {
					field.ballItem = new ItemStack(Material.SNOW_BALL, 1);
				} else if (ballItem.contains(":")) {
					String[] data = ballItem.split(":");
					field.ballItem = new ItemStack(Integer.parseInt(data[0]), 1, Short.parseShort(data[1]));
				} else {
					field.ballItem = new ItemStack(Integer.parseInt(ballItem));
				}
				
				// kick options
				field.enableKick = node.getBoolean("enable-kick", true);
				field.hKickPower = node.getDouble("horizontal-kick-power", 0.8);
				field.vKickPower = node.getDouble("vertical-kick-power", 0.1);
				
				// throw options
				field.enableThrow = node.getBoolean("enable-throw", true);
				field.throwPower = node.getDouble("throw-power", 0.5);
				
				// dispense options
				field.enableDispense = node.getBoolean("enable-dispense", false);
				field.dispensePower = node.getDouble("dispense-power", 1.0);
				
				// batting options
				field.enableBat = node.getBoolean("enable-batting", false);
				String batItem = node.getString("bat-item");
				if (batItem == null || batItem.equals("")) {
					field.batItem = new ItemStack(Material.STICK, 1);
				} else if (batItem.contains(":")) {
					String[] data = batItem.split(":");
					field.batItem = new ItemStack(Integer.parseInt(data[0]), 1, Short.parseShort(data[1]));				
				} else {
					field.batItem = new ItemStack(Integer.parseInt(batItem));				
				}
				field.batDelay = node.getInt("bat-delay", 500);
				field.batPower = node.getDouble("bat-power", 2.5);
				
				// misc options
				field.fire = node.getBoolean("enable-fire", false);
				field.pickupDelay = node.getInt("pickup-delay", 20);
				
				fields.add(field);
			}
		}
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}

}
