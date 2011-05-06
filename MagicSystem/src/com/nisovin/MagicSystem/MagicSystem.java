package com.nisovin.MagicSystem;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSystem.Spells.*;

public class MagicSystem extends JavaPlugin {

	public static int broadcastRange;
	public static String strOnCooldown;
	public static String strMissingReagents;
	public static int wandSpellItem;
	
	public static HashMap<String,Spell> spells = new HashMap<String,Spell>();
	public static HashMap<String,Spellbook> spellbooks = new HashMap<String,Spellbook>();
	
	public static HashSet<Spell> entityDamageListeners = new HashSet<Spell>();
	
	@Override
	public void onEnable() {
		
		// make sure directories are created
		this.getDataFolder().mkdir();
		new File(this.getDataFolder(), "spellbooks").mkdir();
		
		// load config
		Configuration config = this.getConfiguration();
		broadcastRange = config.getInt("general.broadcast-range", 20);
		wandSpellItem = config.getInt("general.wand-item", 283);
		strOnCooldown = config.getString("str-on-cooldown", "That spell is on cooldown.");
		strMissingReagents = config.getString("str-missing-reagents", "You do not have the reagents for that spell.");
		
		// load spells
		BlinkSpell.load(config);
		ZapSpell.load(config);
		LightningSpell.load(config);
		
		// load online player spellbooks
		for (Player p : getServer().getOnlinePlayers()) {
			spellbooks.put(p.getName(), new Spellbook(p, this));
		}
		
		new MagicPlayerListener(this);
		
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		if (sender instanceof Player && command.getName().equalsIgnoreCase("/cast")) {
			Player player = (Player)sender;
			if (args.length == 0) {
				// TODO: show help
			} else {
				Spellbook spellbook = spellbooks.get(player.getName());
				Spell spell = null;
				if (spellbook != null) {
					spell = spellbook.getSpellByName(args[0]);
				}
				if (spell != null) {
					String[] spellArgs = null;
					if (args.length > 1) {
						spellArgs = new String[args.length-1];
						for (int i = 1; i < args.length; i++) {
							spellArgs[i-1] = args[i];
						}
					}
					spell.cast(player, spellArgs);
				} else {
					// TODO: send unknown spell message
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void onDisable() {
		// TODO Auto-generated method stub
		
	}
	
}
