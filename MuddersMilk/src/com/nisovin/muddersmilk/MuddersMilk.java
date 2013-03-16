package com.nisovin.muddersmilk;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.Configuration;

public class MuddersMilk extends JavaPlugin {

	private Map<String,Integer> drunks;
	private DrunkEffects effects;
	private int taskId;
	
	public int itemId;
	public int itemData;
	public int itemEmptyId;
	public int itemEmptyData;
	public int tipsyLevel;
	public int smashedLevel;
	public int poisoningLevel;
	public String tipsyStr;
	public String smashedStr;
	public String poisoningStr;
	public String soberStr;
	public boolean soberOnDeath;
	public boolean soberOnLogout;
	public boolean destroyBucketOnUse;
	public int visionBlurDurationPerLevel;
	public int chanceToSlurS;
	public int chanceToSlurSPerLevel;
	public int chanceToHic;
	public int chanceToHicPerLevel;
	public int effectTickInterval;
	public int chanceToStagger;
	public int chanceToStaggerPerLevel;
	public int staggerIntensity;
	public int chanceToDropItem;
	public int chanceToDropItemPerLevel;
	public int chanceToSober;
	
	@Override
	public void onEnable() {
		drunks = Collections.synchronizedMap(new HashMap<String,Integer>());
		
		loadConfigFromJar();
		Configuration config = getConfig();
		itemId = config.getInt("item-id", Material.MILK_BUCKET.getId());
		itemData = config.getInt("item-data", 0);
		itemEmptyId = config.getInt("empty-item-id", Material.BUCKET.getId());
		itemEmptyData = config.getInt("empty-item-data", 0);
		tipsyLevel = config.getInt("tipsy-level", 1);
		smashedLevel = config.getInt("smashed-level", 5);
		poisoningLevel = config.getInt("poisoning-level", 10);
		tipsyStr = config.getString("tipsy-string", "You are feeling tipsy.");
		smashedStr = config.getString("smashed-string", "You are completely smashed.");
		poisoningStr = config.getString("poisoning-string", "You are getting alcohol poisoning!");
		soberStr = config.getString("sober-str", "You are now sober.");
		soberOnDeath = config.getBoolean("sober-on-death", false);
		soberOnLogout = config.getBoolean("sober-on-logout", true);
		destroyBucketOnUse = config.getBoolean("destroy-bucket-on-use", false);
		visionBlurDurationPerLevel = config.getInt("vision-blur-duration-per-level", 50);
		chanceToSlurS = config.getInt("chance-to-slur-s", 50);
		chanceToSlurSPerLevel = config.getInt("chance-to-slur-s-per-level", 5);
		chanceToHic = config.getInt("chance-to-hic", 15);
		chanceToHicPerLevel = config.getInt("chance-to-hic-per-level", 5);
		effectTickInterval = config.getInt("effect-tick-interval", 100);
		chanceToStagger = config.getInt("chance-to-stagger", 30);
		chanceToStaggerPerLevel = config.getInt("chance-to-stagger-per-level", 10);
		staggerIntensity = config.getInt("stagger-intensity", 5);
		chanceToDropItem = config.getInt("chance-to-drop-item", 10);
		chanceToDropItemPerLevel = config.getInt("chance-to-drop-item-per-level", 2);
		chanceToSober = config.getInt("chance-to-sober-up-one-level", 15);
		
		new MilkPlayerListener(this);
	}
	
	public int moreDrunk(Player player) {
		Integer lvl = drunks.get(player.getName());
		if (lvl == null) {
			drunks.put(player.getName(), 1);
			lvl = 1;
		} else {
			drunks.put(player.getName(), lvl+1);
			lvl = lvl+1;
		}
		if (effects == null) {
			startEffects();
		}
		return lvl;
	}
	
	public Map<String,Integer> getDrunks() {
		return drunks;
	}
	
	public int getDrunkLevel(Player player) {
		Integer lvl = drunks.get(player.getName());
		if (lvl == null) {
			return 0;
		} else {
			return lvl;
		}
	}
	
	public void startEffects() {
		effects = new DrunkEffects(this);
		taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, effects, effectTickInterval, effectTickInterval);
	}
	
	public void stopEffects() {
		getServer().getScheduler().cancelTask(taskId);
		effects = null;
	}

	public void loadConfigFromJar() {
		File folder = this.getDataFolder();
		if (!folder.exists()) {
			folder.mkdir();
		}
		File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
        	this.saveDefaultConfig();
        }
	}
	
	@Override
	public void onDisable() {
		if (effects != null) {
			stopEffects();
		}
	}

}
