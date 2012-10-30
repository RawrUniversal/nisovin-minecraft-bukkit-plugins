package com.nisovin.magicspells.spells.command;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.CraftBukkitHandle;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.Spellbook;
import com.nisovin.magicspells.spells.CommandSpell;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.SpellReagents;
import com.nisovin.magicspells.util.Util;

public class ScrollSpell extends CommandSpell {

	private boolean castForFree;
	private boolean ignoreCastPerm;
	private boolean bypassNormalChecks;
	private int defaultUses;
	private int maxUses;
	private int itemId;
	private boolean rightClickCast;
	private boolean leftClickCast;
	private boolean removeScrollWhenDepleted;
	private boolean chargeReagentsForSpellPerCharge;
	private boolean requireTeachPerm;
	private boolean requireScrollCastPermOnUse;
	private boolean textContainsUses;
	private String strScrollName;
	private String strScrollSubtext;
	private String strUsage;
	private String strNoSpell;
	private String strCantTeach;
	private String strOnUse;
	private String strUseFail;
		
	public ScrollSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		castForFree = getConfigBoolean("cast-for-free", true);
		ignoreCastPerm = getConfigBoolean("ignore-cast-perm", false);
		bypassNormalChecks = getConfigBoolean("bypass-normal-checks", false);
		defaultUses = getConfigInt("default-uses", 5);
		maxUses = getConfigInt("max-uses", 10);
		itemId = getConfigInt("item-id", Material.PAPER.getId());
		rightClickCast = getConfigBoolean("right-click-cast", true);
		leftClickCast = getConfigBoolean("left-click-cast", false);
		removeScrollWhenDepleted = getConfigBoolean("remove-scroll-when-depleted", true);
		chargeReagentsForSpellPerCharge = getConfigBoolean("charge-reagents-for-spell-per-charge", false);
		requireTeachPerm = getConfigBoolean("require-teach-perm", true);
		requireScrollCastPermOnUse = getConfigBoolean("require-scroll-cast-perm-on-use", true);
		strScrollName = getConfigString("str-scroll-name", "Magic Scroll: %s");
		strScrollSubtext = getConfigString("str-scroll-subtext", "Uses remaining: %u");
		strUsage = getConfigString("str-usage", "You must hold a single blank paper \nand type /cast scroll <spell> <uses>.");
		strNoSpell = getConfigString("str-no-spell", "You do not know a spell by that name.");
		strCantTeach = getConfigString("str-cant-teach", "You cannot create a scroll with that spell.");
		strOnUse = getConfigString("str-on-use", "Spell Scroll: %s used. %u uses remaining.");
		strUseFail = getConfigString("str-use-fail", "Unable to use this scroll right now.");
		
		textContainsUses = strScrollName.contains("%u") || strScrollSubtext.contains("%u");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args == null || args.length == 0) {
				// fail -- no args
				sendMessage(player, strUsage);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get item in hand
			ItemStack inHand = player.getItemInHand();
			if (inHand.getTypeId() != itemId || inHand.getAmount() != 1) {
				// fail -- incorrect item in hand
				sendMessage(player, strUsage);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get spell
			Spell spell = MagicSpells.getSpellByInGameName(args[0]);
			Spellbook spellbook = MagicSpells.getSpellbook(player);
			if (spell == null || spellbook == null || !spellbook.hasSpell(spell)) {
				// fail -- no such spell
				sendMessage(player, strNoSpell);
				return PostCastAction.ALREADY_HANDLED;			
			} else if (requireTeachPerm && !spellbook.canTeach(spell)) {
				sendMessage(player, strCantTeach);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			// get uses
			int uses = defaultUses;
			if (args.length > 1 && args[1].matches("^-?[0-9]+$")) {
				uses = Integer.parseInt(args[1]);
			}
			if (uses > maxUses || (maxUses > 0 && uses < 0)) {
				uses = maxUses;
			}
			
			// get additional reagent cost
			if (chargeReagentsForSpellPerCharge && uses > 0) {
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
			
			// create scroll
			inHand = createScroll(spell, uses, inHand);
			player.setItemInHand(inHand);
			
			// done
			sendMessage(player, formatMessage(strCastSelf, "%s", spell.getName()));
			return PostCastAction.NO_MESSAGES;
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	public ItemStack createScroll(Spell spell, int uses, ItemStack item) {
		if (item == null) item = new ItemStack(itemId, 1);
		CraftBukkitHandle handle = MagicSpells.getVolatileCodeHandler();
		item = handle.setStringOnItemStack(item, "MagicSpellsScroll_" + internalName, spell.getInternalName() + (uses > 0 ? "," + uses : ""));
		item = handle.setItemName(item, strScrollName.replace("%s", spell.getName()).replace("%u", uses+""));
		if (strScrollSubtext != null && !strScrollSubtext.isEmpty()) {
			item = handle.setItemLore(item, strScrollSubtext.replace("%s", spell.getName()).replace("%u", uses+""));
		}
		handle.addFakeEnchantment(item);
		return item;
	}
	
	@Override
	public boolean castFromConsole(CommandSender sender, String[] args) {
		return false;
	}
	
	@Override
	public List<String> tabComplete(CommandSender sender, String partial) {
		String[] args = Util.splitParams(partial);
		if (args.length == 1) {
			return tabCompleteSpellName(sender, args[0]);
		} else {
			return null;
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if ((rightClickCast && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) ||
			(leftClickCast && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))) {
			Player player = event.getPlayer();
			ItemStack inHand = player.getItemInHand();
			if (inHand.getTypeId() != itemId) return;
			
			// get scroll data (spell and uses)
			String scrollDataString = MagicSpells.getVolatileCodeHandler().getStringOnItemStack(inHand, "MagicSpellsScroll_" + internalName);
			if (scrollDataString == null || scrollDataString.isEmpty()) return;
			String[] scrollData = scrollDataString.split(",");
			Spell spell = MagicSpells.getSpellByInternalName(scrollData[0]);
			if (spell == null) return;
			int uses = 0;
			if (scrollData.length > 1 && scrollData[1].matches("^[0-9]+$")) {
				uses = Integer.parseInt(scrollData[1]);
			}			
	
			// check for permission
			if (requireScrollCastPermOnUse && !MagicSpells.getSpellbook(player).canCast(this)) {
				sendMessage(player, strUseFail);
				return;
			}
					
			
			// cast spell
			if (ignoreCastPerm && !player.hasPermission("magicspells.cast." + spell.getInternalName())) {
				player.addAttachment(MagicSpells.plugin, "magicspells.cast." + spell.getInternalName(), true, 1);
			}
			if (castForFree && !player.hasPermission("magicspells.noreagents")) {
				player.addAttachment(MagicSpells.plugin, "magicspells.noreagents", true, 1);
			}
			SpellCastState state;
			PostCastAction action;
			if (bypassNormalChecks) {
				state = SpellCastState.NORMAL;
				action = spell.castSpell(player, SpellCastState.NORMAL, 1.0F, null);
			} else {
				SpellCastResult result = spell.cast(player);
				state = result.state;
				action = result.action;
			}

			if (state == SpellCastState.NORMAL && action != PostCastAction.ALREADY_HANDLED) {
				// remove use
				if (uses > 0) {
					uses -= 1;
					if (uses > 0) {
						inHand = createScroll(spell, uses, inHand);
						if (textContainsUses) {
							player.setItemInHand(inHand);
						}
					} else {
						if (removeScrollWhenDepleted) {
							player.setItemInHand(null);
						} else {
							player.setItemInHand(new ItemStack(itemId, 1));
						}
					}
				}
				
				// send msg
				sendMessage(player, formatMessage(strOnUse, "%s", spell.getName(), "%u", (uses>=0?uses+"":"many")));
			}
		}
	}

}
