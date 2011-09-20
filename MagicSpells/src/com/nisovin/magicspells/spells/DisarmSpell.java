package com.nisovin.magicspells.spells;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.EntityItem;

import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;

import com.nisovin.magicspells.InstantSpell;

public class DisarmSpell extends InstantSpell {

	private List<Integer> disarmable;
	private int disarmDuration;
	private boolean obeyLos;
	
	private String strNoTarget;
	private String strInvalidItem;
	private String strCastTarget;
	
	public DisarmSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		disarmable = new ArrayList<Integer>();
		disarmable.add(280);
		disarmable = getConfigIntList("disarmable-items", disarmable);
		
		disarmDuration = getConfigInt("disarm-duration", 100);
		obeyLos = getConfigBoolean("obey-los", true);
		strNoTarget = getConfigString("str-no-target", "No target found.");
		strInvalidItem = getConfigString("str-invalid-item", "Your target could not be disarmed.");
		strCastTarget = getConfigString("str-cast-target", "%a has disarmed you.");
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get target
			Player target = getTargetedPlayer(player, range, obeyLos);
			if (target == null) {
				// fail
				sendMessage(player, strNoTarget);
				return PostCastAction.ALREADY_HANDLED;
			}
			
			ItemStack inHand = target.getItemInHand();
			if (disarmable.contains(inHand.getTypeId())) {
				// drop item
				target.setItemInHand(null);
				Item item = target.getWorld().dropItemNaturally(target.getLocation(), inHand.clone());
				((EntityItem)((CraftItem)item).getHandle()).pickupDelay = disarmDuration;
				// send messages
				sendMessage(player, strCastSelf, "%t", target.getDisplayName());
				sendMessage(target, strCastTarget, "%a", player.getDisplayName());
				sendMessageNear(player, formatMessage(strCastOthers, "%t", target.getDisplayName(), "%a", player.getDisplayName()));
				return PostCastAction.NO_MESSAGES;
			} else {
				// fail
				sendMessage(player, strInvalidItem);
				return PostCastAction.ALREADY_HANDLED;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

}
