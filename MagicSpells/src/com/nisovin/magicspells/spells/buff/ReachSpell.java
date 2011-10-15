package com.nisovin.magicspells.spells.buff;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.BuffSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ReachSpell extends BuffSpell {

	private int range;
	private boolean consumeBlocks;
	private boolean dropBlocks;
	
	private HashSet<Player> reaching;
	
	public ReachSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		range = getConfigInt("range", 25);
		consumeBlocks = getConfigBoolean("consume-blocks", true);
		dropBlocks = getConfigBoolean("drop-blocks", true);
		
		addListener(Event.Type.PLAYER_INTERACT);
		
		reaching = new HashSet<Player>();
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (reaching.contains(player)) {
			turnOff(player);
		} else if (state == SpellCastState.NORMAL) {
			reaching.add(player);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (reaching.contains(event.getPlayer())) {
			Player player = event.getPlayer();
			Action action = event.getAction();
			List<Block> targets = player.getLastTwoTargetBlocks(null, range);
			Block airBlock, targetBlock;
			if (targets.size() == 2) {
				airBlock = targets.get(0);
				targetBlock = targets.get(1);
				if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
					// break
					BlockBreakEvent evt = new BlockBreakEvent(targetBlock, player);
					Bukkit.getPluginManager().callEvent(evt);
					if (!evt.isCancelled()) {
						BlockState state = targetBlock.getState();
						targetBlock.getWorld().playEffect(targetBlock.getLocation(), Effect.STEP_SOUND, targetBlock.getTypeId());
						targetBlock.setType(Material.AIR);
						// drop item
						if (dropBlocks && player.getGameMode() == GameMode.SURVIVAL) {
							Random rand = new Random();
							byte data = state.getRawData();
							int type = net.minecraft.server.Block.byId[state.getTypeId()].a(data, rand);
							int amt = net.minecraft.server.Block.byId[state.getTypeId()].a(rand);
							if (amt > 0) {
								targetBlock.getWorld().dropItemNaturally(targetBlock.getLocation(), new ItemStack(type, amt, data));
							}
						}
					}
				} else if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && targetBlock.getType() != Material.AIR) {
					// place
					ItemStack inHand = player.getItemInHand();
					if (inHand != null && inHand.getType() != Material.AIR && inHand.getType().isBlock()) {
						BlockState prevState = airBlock.getState();
						byte data = 0;
						if (inHand.getData() != null) {
							data = inHand.getData().getData();
						}
						airBlock.setTypeIdAndData(inHand.getTypeId(), data, true);
						BlockPlaceEvent evt = new BlockPlaceEvent(airBlock, prevState, targetBlock, inHand, player, true);
						Bukkit.getPluginManager().callEvent(evt);
						if (evt.isCancelled()) {
							// cancelled, revert
							prevState.update(true);
						} else {
							// remove item from hand
							if (consumeBlocks && player.getGameMode() == GameMode.SURVIVAL) {
								if (inHand.getAmount() > 1) {
									inHand.setAmount(inHand.getAmount() - 1);
									player.setItemInHand(inHand);
								} else {
									player.setItemInHand(null);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		reaching.remove(player);
		sendMessage(player, strFade);
	}
	
	@Override
	protected void turnOff() {
	}



}
