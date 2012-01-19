package com.nisovin.magicspells.shop;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;

public class MagicSpellsShop extends JavaPlugin implements Listener {

	private String firstLine;
	private boolean requireKnownSpell;
	private boolean requireTeachPerm;
	private String strAlreadyKnown;
	private String strCantAfford;
	private String strPurchased;
	
	private Economy economy;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		Configuration config = getConfig();
		firstLine = config.getString("first-line", "[SPELL SHOP]");
		requireKnownSpell = config.getBoolean("require-known-spell", true);
		requireTeachPerm = config.getBoolean("require-teach-perm", true);
		strAlreadyKnown = config.getString("str-already-known", "You already know that spell.");
		strCantAfford = config.getString("str-cant-afford", "You cannot afford that spell.");
		strPurchased = config.getString("str-purchased", "You have purchased the %s spell.");
		
		// set up economy hook
		RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
		if (provider != null) {
			economy = provider.getProvider();
		} else {		
			getLogger().severe("Vault economy provider could not be found!");
		}
		
		// register events
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler(event=PlayerInteractEvent.class, priority=EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) return;
		
		// check for right-click on sign
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}		
		Block block = event.getClickedBlock();
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
			return;
		}
		
		// get shop sign
		Sign sign = (Sign)block.getState();
		String[] lines = sign.getLines();		
		if (!lines[0].equals(firstLine)) {
			return;
		}
		
		Player player = event.getPlayer();
		
		// get spell
		String spellName = lines[1];
		Spell spell = MagicSpells.getSpellByInGameName(spellName);
		if (spell == null) {
			return;
		}
		
		// check if already known
		Spellbook spellbook = MagicSpells.getSpellbook(player);
		if (spellbook.hasSpell(spell)) {
			MagicSpells.sendMessage(player, strAlreadyKnown);
			return;
		}
		
		// get cost
		double cost = 0;
		if (!lines[2].isEmpty()) {
			if (!lines[2].contains(" ") && lines[2].matches("^[0-9]+(\\.[0-9]+)?$")) {
				cost = Integer.parseInt(lines[2]);
			} else if (lines[2].contains(" ")) {
				String[] s = lines[2].split(" ");
				if (s[0].matches("^[0-9]+(\\.[0-9]+)?$")) {
					cost = Double.parseDouble(s[0]);
				}
			}
		}
		
		// check for currency
		if (economy == null || !economy.has(player.getName(), cost)) {
			MagicSpells.sendMessage(player, strCantAfford);
			return;
		}
		
		// attempt to teach
		boolean taught = MagicSpells.teachSpell(player, spellName);
		if (!taught) {
			return;
		}
		
		// remove currency
		economy.withdrawPlayer(player.getName(), cost);
		
		// success!
		MagicSpells.sendMessage(player, strPurchased, "%s", spellName);
	}
	
	@EventHandler(event=SignChangeEvent.class, priority=EventPriority.NORMAL)
	public void onSignCreate(SignChangeEvent event) {
		if (event.isCancelled()) return;
		
		String lines[] = event.getLines();
		if (!lines[0].equals(firstLine)) {
			return;
		}
		
		// check permission
		if (!event.getPlayer().hasPermission("magicspells.createsignshop")) {
			event.setCancelled(true);
			return;
		}
		
		// check for valid spell
		String spellName = lines[1];
		Spell spell = MagicSpells.getSpellByInGameName(spellName);
		if (spell == null) {
			event.getPlayer().sendMessage("A spell by that name does not exist.");
		}
		
		// check permissions
		Spellbook spellbook = MagicSpells.getSpellbook(event.getPlayer());
		if (requireKnownSpell && !spellbook.hasSpell(spell)) {
			event.setCancelled(true);
			return;
		}
		if (requireTeachPerm && !spellbook.canTeach(spell)) {
			event.setCancelled(true);
			return;
		}
		
		// get cost
		double cost = 0;
		if (!lines[2].isEmpty()) {
			if (!lines[2].contains(" ") && lines[2].matches("^[0-9]+(\\.[0-9]+)?$")) {
				cost = Integer.parseInt(lines[2]);
			} else if (lines[2].contains(" ")) {
				String[] s = lines[2].split(" ");
				if (s[0].matches("^[0-9]+(\\.[0-9]+)?$")) {
					cost = Double.parseDouble(s[0]);
				}
			}
		}
		
		event.getPlayer().sendMessage("Spell shop created: " + spellName + " for " + cost + " currency.");
		
	}

	@Override
	public void onDisable() {
		
	}

}
