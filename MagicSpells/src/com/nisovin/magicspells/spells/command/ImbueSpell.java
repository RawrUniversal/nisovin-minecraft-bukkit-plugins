package com.nisovin.magicspells.spells.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.ItemNameResolver;
import com.nisovin.magicspells.util.ItemNameResolver.ItemTypeAndData;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellReagents;

public class ImbueSpell extends CommandSpell {

	private int defaultUses;
	private int maxUses;
	private boolean allowSpecifyUses;
	private boolean chargeReagentsForSpellPerUse;
	private boolean requireTeachPerm;
	private boolean consumeItem;
	private Set<Integer> allowedItems;
	
	private String strUsage;
	private String strCantImbueItem;
	private String strCantImbueSpell;
	
	public ImbueSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		defaultUses = getConfigInt("default-uses", 5);
		maxUses = getConfigInt("max-uses", 10);
		allowSpecifyUses = getConfigBoolean("allow-specify-uses", true);
		chargeReagentsForSpellPerUse = getConfigBoolean("charge-reagents-for-spell-per-use", true);
		requireTeachPerm = getConfigBoolean("require-teach-perm", true);
		consumeItem = getConfigBoolean("consume-item", false);

		allowedItems = new HashSet<Integer>();
		List<String> allowed = getConfigStringList("allowed-items", null);
		if (allowed != null) {
			ItemNameResolver resolver = MagicSpells.getItemNameResolver();
			for (String s : allowed) {
				ItemTypeAndData type = resolver.resolve(s);
				if (type != null) {
					allowedItems.add(type.id);
				}
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				// usage
				sendMessage(player, strUsage);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get item
			ItemStack inHand = player.getItemInHand();
			if (!allowedItems.contains(inHand.getTypeId())) {
				// disallowed item
				sendMessage(player, strCantImbueItem);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// check for already imbued
			if (MagicSpells.getVolatileCodeHandler().getStringOnItemStack(inHand, "MagicSpellsImbue_" + internalName) != null) {
				// already imbued
				sendMessage(player, strCantImbueItem);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get spell
			Spell spell = MagicSpells.getSpellByInGameName(args[0]);
			if (spell == null) {
				// no spell
				sendMessage(player, strCantImbueSpell);
				return PostCastAction.ALREADY_HANDLED;
			}
			if (!MagicSpells.getSpellbook(player).hasSpell(spell)) {
				// doesn't know spell
				sendMessage(player, strCantImbueSpell);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// check teach perm
			if (requireTeachPerm && !MagicSpells.getSpellbook(player).canTeach(spell)) {
				// can't teach
				sendMessage(player, strCantImbueSpell);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get uses
			int uses = defaultUses;
			if (args.length > 1 && args[1].matches("[0-9]+") && (allowSpecifyUses || player.hasPermission("magicspells.advanced.imbue"))) {
				uses = Integer.parseInt(args[1]);
				if (uses > maxUses) {
					uses = maxUses;
				} else if (uses <= 0) {
					uses = 1;
				}
			}
			
			// get additional reagent cost
			if (chargeReagentsForSpellPerUse && !player.hasPermission("magicspells.noreagents")) {
				SpellReagents reagents = spell.getReagents().multiply(uses);
				if (!hasReagents(player, reagents)) {
					// missing reagents
					sendMessage(player, strMissingReagents);
					return PostCastAction.ALREADY_HANDLED;
				} else {
					// has reagents, so just remove them
					removeReagents(player, reagents);
				}
			}
			
			// imbue item
			MagicSpells.getVolatileCodeHandler().setStringOnItemStack(inHand, "MagicSpellsImbue_" + internalName, spell.getInternalName() + "," + uses);
			player.setItemInHand(inHand);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event) {
		if (event.hasItem() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			ItemStack item = event.getItem();
			if (allowedItems.contains(item.getTypeId())) {
				String imbueData = MagicSpells.getVolatileCodeHandler().getStringOnItemStack(item, "MagicSpellsImbue_" + internalName);
				if (imbueData != null && !imbueData.isEmpty()) {
					String[] data = imbueData.split(",");
					Spell spell = MagicSpells.getSpellByInternalName(data[0]);
					int uses = Integer.parseInt(data[1]);
					
					if (spell != null && uses > 0) {
						spell.castSpell(event.getPlayer(), SpellCastState.NORMAL, 1.0F, null);
						uses--;
						if (uses <= 0) {
							MagicSpells.getVolatileCodeHandler().removeStringOnItemStack(item, "MagicSpellsImbue_" + internalName);
							if (consumeItem) {
								event.getPlayer().setItemInHand(null);
							}
						} else {
							MagicSpells.getVolatileCodeHandler().setStringOnItemStack(item, "MagicSpellsImbue_" + internalName, spell.getInternalName() + "," + uses);
						}						
					} else {
						MagicSpells.getVolatileCodeHandler().removeStringOnItemStack(item, "MagicSpellsImbue_" + internalName);
					}
				}
			}
		}
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] tabComplete(CommandSender sender, String partial) {
		// TODO Auto-generated method stub
		return null;
	}

}
