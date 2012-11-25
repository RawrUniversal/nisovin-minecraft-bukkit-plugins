package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.ItemNameResolver.ItemTypeAndData;
import com.nisovin.magicspells.util.MagicConfig;

public class ThrowBlockSpell extends InstantSpell {

	int blockType;
	byte blockData;
	float velocity;
	float fallDamage;
	int fallDamageMax;
	boolean dropItem;
	boolean removeBlocks;
	boolean callTargetEvent;
	boolean checkPlugins;
	
	Map<FallingBlock, FallingBlockInfo> fallingBlocks;
	int cleanTask = -1;
	
	public ThrowBlockSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		String blockTypeInfo = getConfigString("block-type", Material.ANVIL.getId() + "");
		ItemTypeAndData typeAndData = MagicSpells.getItemNameResolver().resolve(blockTypeInfo);
		blockType = typeAndData.id;
		blockData = (byte)typeAndData.data;
		velocity = getConfigFloat("velocity", 1);
		fallDamage = getConfigFloat("fall-damage", 2.0F);
		fallDamageMax = getConfigInt("fall-damage-max", 20);
		dropItem = getConfigBoolean("drop-item", false);
		removeBlocks = getConfigBoolean("remove-blocks", false);
		callTargetEvent = getConfigBoolean("call-target-event", true);
		checkPlugins = getConfigBoolean("check-plugins", false);
	}	
	
	@Override
	public void initialize() {
		if (fallDamage > 0 || removeBlocks) {
			fallingBlocks = new HashMap<FallingBlock, ThrowBlockSpell.FallingBlockInfo>();
			if (fallDamage > 0) {
				registerEvents(this);
			}
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			FallingBlock block = player.getWorld().spawnFallingBlock(player.getEyeLocation().add(player.getLocation().getDirection()), blockType, blockData);
			block.setVelocity(player.getLocation().getDirection().multiply(velocity));
			block.setDropItem(dropItem);
			if (fallDamage > 0) {
				MagicSpells.getVolatileCodeHandler().setFallingBlockHurtEntities(block, fallDamage, fallDamageMax);
			}
			if (fallingBlocks != null) {
				fallingBlocks.put(block, new FallingBlockInfo(player, power));
				if (cleanTask < 0) {
					startTask();
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private void startTask() {
		cleanTask = Bukkit.getScheduler().scheduleSyncDelayedTask(MagicSpells.plugin, new Runnable() {
			public void run() {
				Iterator<FallingBlock> iter = fallingBlocks.keySet().iterator();
				while (iter.hasNext()) {
					FallingBlock block = iter.next();
					if (!block.isValid()) {
						iter.remove();
						if (removeBlocks) {
							Block b = block.getLocation().getBlock();
							if (b.getTypeId() == blockType && b.getData() == blockData) {
								b.setType(Material.AIR);
							}
						}
					}
				}
				if (fallingBlocks.size() == 0) {
					cleanTask = -1;
				} else {
					startTask();
				}
			}
		}, 100);
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onDamage(EntityDamageByEntityEvent event) {
		FallingBlockInfo info = null;
		if (removeBlocks) {
			info = fallingBlocks.get(event.getDamager());
		} else {
			info = fallingBlocks.remove(event.getDamager());
		}
		if (info != null && event.getEntity() instanceof LivingEntity) {
			int damage = Math.round(event.getDamage() * info.power);
			if (callTargetEvent) {
				SpellTargetEvent evt = new SpellTargetEvent(this, info.player, (LivingEntity)event.getEntity());
				Bukkit.getPluginManager().callEvent(evt);
				if (evt.isCancelled()) {
					event.setCancelled(true);
					return;
				}
			}
			if (checkPlugins) {
				EntityDamageByEntityEvent evt = new EntityDamageByEntityEvent(info.player, event.getEntity(), DamageCause.ENTITY_ATTACK, damage);
				Bukkit.getPluginManager().callEvent(evt);
				if (evt.isCancelled()) {
					event.setCancelled(true);
					return;
				}
			}
			event.setDamage(damage);
		}
	}
	
	@Override
	public void turnOff() {
		if (fallingBlocks != null) {
			fallingBlocks.clear();
		}
	}
	
	class FallingBlockInfo {
		Player player;
		float power;
		public FallingBlockInfo(Player player, float power) {
			this.player = player;
			this.power = power;
		}
	}

}
