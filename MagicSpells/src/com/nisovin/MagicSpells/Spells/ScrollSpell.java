package com.nisovin.MagicSpells.Spells;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.CommandSpell;
import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;

public class ScrollSpell extends CommandSpell {

	private boolean castForFree;
	private int defaultUses;
	private int maxUses;
	private int itemId;
	private boolean rightClickCast;
	private boolean leftClickCast;
	private boolean ignoreCastPerm;
	private boolean setUnstackable;
	private boolean chargeReagentsForSpellPerCharge;
	private String stackByDataVar;
	private int maxScrolls;
	private String strScrollOver;
	private String strUsage;
	private String strFail;
	private String strNoSpell;
	private String strOnUse;
	
	private HashMap<Short,Spell> scrollSpells;
	private HashMap<Short,Integer> scrollUses;
	private boolean dirtyData;
	
	public ScrollSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		castForFree = getConfigBoolean(config, "cast-for-free", true);
		defaultUses = getConfigInt(config, "default-uses", 5);
		maxUses = getConfigInt(config, "max-uses", 10);
		itemId = getConfigInt(config, "item-id", Material.PAPER.getId());
		rightClickCast = getConfigBoolean(config, "right-click-cast", true);
		leftClickCast = getConfigBoolean(config, "left-click-cast", false);
		ignoreCastPerm = getConfigBoolean(config, "ignore-cast-perm", false);
		setUnstackable = getConfigBoolean(config, "set-unstackable", true);
		chargeReagentsForSpellPerCharge = getConfigBoolean(config, "charge-reagents-for-spell-per-charge", false);
		stackByDataVar = getConfigString(config, "stack-by-data-var", "bj");
		maxScrolls = getConfigInt(config, "max-scrolls", 500);
		strScrollOver = getConfigString(config, "str-scroll-over", "Spell Scroll: %s (%u uses remaining)");
		strUsage = getConfigString(config, "str-usage", "You must hold a single blank paper \nand type /cast scroll <spell> <uses>.");
		strFail = getConfigString(config, "str-fail", "You cannot create a spell scroll at this time.");
		strNoSpell = getConfigString(config, "str-no-spell", "You do not know a spell by that name.");
		strOnUse = getConfigString(config, "str-on-use", "Spell Scroll: %s used. %u uses remaining.");
		
		addListener(Event.Type.PLAYER_INTERACT);
		addListener(Event.Type.PLAYER_ITEM_HELD);
		
		scrollSpells = new HashMap<Short,Spell>();
		scrollUses = new HashMap<Short,Integer>();
		
		// prevent paper stacking
		if (setUnstackable) {
			try {
				boolean ok = false;
				try {
					// attempt to make books with different data values stack separately
					Field field1 = net.minecraft.server.Item.class.getDeclaredField(stackByDataVar);
					if (field1.getType() == boolean.class) {
						field1.setAccessible(true);
						field1.setBoolean(net.minecraft.server.Item.byId[itemId], true);
						ok = true;
					} 
				} catch (Exception e) {
				}
				if (!ok) {
					// otherwise limit stack size to 1
					Field field2 = net.minecraft.server.Item.class.getDeclaredField("maxStackSize");
					field2.setAccessible(true);
					field2.setInt(net.minecraft.server.Item.byId[itemId], 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void initialize() {
		load();
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				// fail -- no args
				sendMessage(player, strUsage);
				return true;
			} 
			
			// check for base scroll
			if (player.isOp() && args[0].equalsIgnoreCase("-base")) {
				createBaseScroll(args, player);
				return true;
			}
			
			// get item in hand
			ItemStack inHand = player.getItemInHand();
			short id = inHand.getDurability();
			if (inHand.getTypeId() != itemId || inHand.getAmount() != 1 || scrollSpells.containsKey(id)) {
				// fail -- incorrect item in hand
				sendMessage(player, strUsage);
				return true;
			}
			
			// get scroll id
			if (id == 0) {
				id = getNextId();
				if (id == 0) {
					// fail -- no more scroll space
					sendMessage(player, strFail);
					return true;
				}
			}
			
			// get spell
			Spell spell = MagicSpells.getSpellByInGameName(args[0]);
			if (spell == null) {
				// fail -- no such spell
				sendMessage(player, strNoSpell);
				return true;
			}
			
			// get uses
			int uses = defaultUses;
			if (args.length > 1 && args[1].matches("^[0-9]+$")) {
				uses = Integer.parseInt(args[1]);
			}
			if (uses > maxUses || (maxUses > 0 && uses < 0)) {
				uses = maxUses;
			}
			
			// get additional reagent cost
			if (chargeReagentsForSpellPerCharge && uses > 0) {
				ItemStack[] spellReagents = spell.getReagentCost();
				ItemStack[] reagents = new ItemStack[spellReagents.length];
				for (int i = 0; i < reagents.length; i++) {
					ItemStack item = spellReagents[i];
					if (item != null) {
						item = item.clone();
						item.setAmount(item.getAmount() * uses);
					}
					reagents[i] = item;
				}
				int manaCost = spell.getManaCost() * uses;
				int healthCost = spell.getHealthCost() * uses;
				if (!hasReagents(player, reagents, healthCost, manaCost)) {
					// missing reagents
					sendMessage(player, MagicSpells.strMissingReagents);
					return true;
				} else {
					// has reagents, so just remove them
					removeReagents(player, reagents, healthCost, manaCost);
				}
			}
			
			// create scroll
			inHand.setDurability(id);
			player.setItemInHand(inHand);
			scrollSpells.put(id, spell);
			scrollUses.put(id, uses);
			dirtyData = true;
			
			// done
			removeReagents(player);
			setCooldown(player);
			sendMessage(player, formatMessage(strCastSelf, "%s", spell.getName()));
			save();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		if (args.length >= 2 && (args[0].equalsIgnoreCase("base") || args[0].equalsIgnoreCase("-base"))) {
			createBaseScroll(args, sender);
		} else if (args.length == 1 && args[0].equalsIgnoreCase("save")) {
			save();
			sender.sendMessage("Scrolls saved");
		} else if (args.length == 1 && args[0].equalsIgnoreCase("load")) {
			load();
			sender.sendMessage("Scrolls loaded");
		}
		return true;
	}
	
	private void createBaseScroll(String[] args, CommandSender sender) {
		// get spell
		Spell spell = MagicSpells.getSpellByInGameName(args[1]);
		if (spell == null) {
			sender.sendMessage("No such spell");
		}
		
		// get uses
		int uses = defaultUses;
		if (args.length > 2 && args[2].matches("^[0-9]+$")) {
			uses = Integer.parseInt(args[2]);
		}
		
		// get id
		short id = getNextNegativeId();
		if (id == 0) {
			sender.sendMessage("Out of scroll space");
		}
		
		// create scroll
		scrollSpells.put(id, spell);
		scrollUses.put(id, uses);
		dirtyData = true;
		save();
		
		sender.sendMessage("Base scroll created for spell " + spell.getName() + ": id = " + id);
	}
	
	private short getNextId() {
		for (short i = 1; i < maxScrolls; i++) {
			if (!scrollSpells.containsKey(i)) {
				return i;
			}
		}
		return 0;
	}
	
	private short getNextNegativeId() {
		for (short i = -1; i > -maxScrolls; i--) {
			if (!scrollSpells.containsKey(i)) {
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((rightClickCast && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) ||
			(leftClickCast && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))) {
			Player player = event.getPlayer();
			ItemStack inHand = player.getItemInHand();
			if (inHand.getTypeId() == itemId && inHand.getDurability() != 0) {		
				short id = inHand.getDurability();
				Spell spell = scrollSpells.get(id);
			
				if (spell != null) {
					// make a copy of the base scroll
					if (id < 0) {
						short newId = getNextId();
						if (newId == 0) {
							// fail -- no more scroll space
							sendMessage(player, strFail);
							return;
						}
						inHand.setDurability(newId);
						player.setItemInHand(inHand);
						scrollSpells.put(newId, spell);
						scrollUses.put(newId, scrollUses.get(id));
						id = newId;
						dirtyData = true;
					}
					
					String name = player.getName().toLowerCase();
					boolean freeCastOverride = (castForFree && !MagicSpells.castForFree.contains(name));
					
					// cast spell
					if (ignoreCastPerm && !player.hasPermission("magicspells.cast." + spell.getInternalName())) {
						player.addAttachment(MagicSpells.plugin, "magicspells.cast." + spell.getInternalName(), true, 1);
					}
					if (freeCastOverride) MagicSpells.castForFree.add(name);
					SpellCastState state = spell.cast(player);
					if (freeCastOverride) MagicSpells.castForFree.remove(name);

					if (state == SpellCastState.NORMAL) {
						// remove use
						int uses = scrollUses.get(id);
						if (uses > 0) {
							uses -= 1;
							if (uses > 0) {
								scrollUses.put(id, uses);
							} else {
								scrollSpells.remove(id);
								scrollUses.remove(id);
							}
							dirtyData = true;
						}
						
						// send msg
						sendMessage(player, formatMessage(strOnUse, "%s", spell.getName(), "%u", (String)(uses>=0?uses:"many")));
					}
				}
			}		
		}
	}
	
	@Override
	public void onItemHeldChange(PlayerItemHeldEvent event) {
		ItemStack inHand = event.getPlayer().getInventory().getItem(event.getNewSlot());
		if (inHand != null && inHand.getTypeId() == itemId && inHand.getDurability() != 0) {
			Spell spell = scrollSpells.get(inHand.getDurability());
			if (spell != null) {
				sendMessage(event.getPlayer(), formatMessage(strScrollOver, "%s", spell.getName(), "%u", scrollUses.get(inHand.getDurability())+""));
			}
		}
	}
	
	@Override
	public void turnOff() {
		save();
	}
	
	private void save() {
		if (dirtyData) {
			MagicSpells.debug("Saving scrolls...");
			File file = new File(MagicSpells.plugin.getDataFolder(), "scrolls.txt");
			if (file.exists()) {
				file.delete();
			}
			Configuration c = new Configuration(file);
			String data;
			for (short i : scrollSpells.keySet()) {
				data = scrollSpells.get(i).getInternalName() + "|" + scrollUses.get(i);
				MagicSpells.debug("    " + i + " : " + data);
				c.setProperty(i+"", data);
			}
			c.save();
		}
	}
	
	private void load() {
		File file = new File(MagicSpells.plugin.getDataFolder(), "scrolls.txt");
		if (file.exists()) {
			MagicSpells.debug("Loading scrolls...");
			Configuration c = new Configuration(file);
			c.load();
			List<String> keys = c.getKeys();
			for (String s : keys) {
				short id = Short.parseShort(s);
				String[] data = c.getString(s).split("\\|");
				MagicSpells.debug("    Raw data: " + c.getString(s));
				Spell spell = MagicSpells.getSpellByInternalName(data[0]);
				int uses = Integer.parseInt(data[1]);
				if (spell != null) {
					scrollSpells.put(id, spell);
					scrollUses.put(id, uses);
					MagicSpells.debug("        Loaded scroll: " + id + " - " + spell.getInternalName() + " - " + uses);
				}
			}
			dirtyData = false;
		}
	}

}
