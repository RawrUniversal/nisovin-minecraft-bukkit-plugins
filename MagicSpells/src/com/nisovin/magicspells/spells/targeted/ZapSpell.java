package com.nisovin.magicspells.spells.targeted;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ZapSpell extends TargetedSpell implements TargetedLocationSpell {
	
	private String strCantZap;
	private HashSet<Byte> transparentBlockTypes;
	private List<Integer> allowedBlockTypes;
	private List<Integer> disallowedBlockTypes;
	private boolean dropBlock;
	private boolean dropNormal;
	private boolean checkPlugins;
	private boolean playBreakEffect;
	
	public ZapSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		strCantZap = getConfigString("str-cant-zap", "");
		String[] transparent = getConfigString("transparent-block-types","0,8,9").split(",");
		String[] allowed = getConfigString("allowed-block-types","").split(",");
		String[] disallowed = getConfigString("disallowed-block-types","0,7,10,11").split(",");
		transparentBlockTypes = new HashSet<Byte>();
		for (String s : transparent) {
			transparentBlockTypes.add(Byte.parseByte(s));
		}
		allowedBlockTypes = new ArrayList<Integer>();
		for (String s : allowed) {
			if (!s.isEmpty()) {
				allowedBlockTypes.add(Integer.parseInt(s));
			}
		}
		disallowedBlockTypes = new ArrayList<Integer>();
		for (String s : disallowed) {
			if (!s.isEmpty()) {
				disallowedBlockTypes.add(Integer.parseInt(s));
			}
		}
		dropBlock = getConfigBoolean("drop-block", false);
		dropNormal = getConfigBoolean("drop-normal", true);
		checkPlugins = getConfigBoolean("check-plugins", true);
		playBreakEffect = getConfigBoolean("play-break-effect", true);
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get targeted block
			Block target;
			try {
				target = player.getTargetBlock(transparentBlockTypes, range>0?range:100);
			} catch (IllegalStateException e) {
				target = null;
			}
			if (target != null) {
				SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, player, target.getLocation());
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					target = null;
				} else {
					target = event.getTargetLocation().getBlock();
				}
			}
			if (target != null) {
				// check for disallowed block
				if (!canZap(target)) {
					return noTarget(player, strCantZap);
				}
				// zap
				boolean ok = zap(target, player);
				if (!ok) {
					return noTarget(player, strCantZap);
				}
			} else {
				return noTarget(player, strCantZap);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean zap(Block target, Player player) {
		// check for protection
		if (checkPlugins) {
			BlockBreakEvent event = new BlockBreakEvent(target, player);
			MagicSpells.plugin.getServer().getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				// a plugin cancelled the event
				return false;
			}
		}
		
		// drop block
		if (dropBlock) {
			if (dropNormal) {
				target.breakNaturally();
			} else {
				target.getWorld().dropItemNaturally(target.getLocation(), new ItemStack(target.getType(), 1, target.getData()));
			}
		}
		
		// show animation
		if (playBreakEffect) {
			target.getWorld().playEffect(target.getLocation(), Effect.STEP_SOUND, target.getTypeId());
		}
		playSpellEffects(EffectPosition.CASTER, player);
		playSpellEffects(EffectPosition.TARGET, target.getLocation());
		playSpellEffectsTrail(player.getLocation(), target.getLocation());
		
		// remove block
		target.setType(Material.AIR);
		return true;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		Block block = target.getBlock();
		if (canZap(block)) {
			zap(block, caster);
			return true;
		} else {
			Vector v = target.getDirection();
			block = target.clone().add(v).getBlock();
			if (canZap(block)) {
				zap(block, caster);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean canZap(Block target) {
		return !(disallowedBlockTypes.contains(target.getTypeId()) || (allowedBlockTypes.size() > 0 && !allowedBlockTypes.contains(target.getTypeId())));
	}
}
