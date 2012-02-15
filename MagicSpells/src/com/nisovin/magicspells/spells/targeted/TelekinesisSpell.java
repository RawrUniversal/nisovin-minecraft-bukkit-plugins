package com.nisovin.magicspells.spells.targeted;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class TelekinesisSpell extends TargetedLocationSpell {

	private String strNoTarget;
	
	private HashSet<Byte> transparent;
	
	public TelekinesisSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You must target a switch or button.");
		
		transparent = new HashSet<Byte>();
		transparent.add((byte)Material.AIR.getId());
		transparent.add((byte)Material.REDSTONE_WIRE.getId());
		transparent.add((byte)Material.REDSTONE_TORCH_ON.getId());
		transparent.add((byte)Material.REDSTONE_TORCH_OFF.getId());
		transparent.add((byte)Material.TORCH.getId());
	}
	
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(transparent, range>0?range:100);
			if (target == null) {
				// fail
				sendMessage(player, strNoTarget);
				fizzle(player);
				return PostCastAction.ALREADY_HANDLED;
			} else {
				boolean activated = activate(target);
				if (!activated) {
					sendMessage(player, strNoTarget);
					fizzle(player);
					return PostCastAction.ALREADY_HANDLED;
				}
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	private boolean activate(Block target) {
		if (target.getType() == Material.LEVER || target.getType() == Material.STONE_BUTTON) {
			net.minecraft.server.Block.byId[target.getType().getId()].interact(((CraftWorld)target.getWorld()).getHandle(), target.getX(), target.getY(), target.getZ(), null);
			return true;
		} else if (target.getType() == Material.WOOD_PLATE || target.getType() == Material.STONE_PLATE) {
			target.setData((byte) (target.getData() ^ 0x1));
			net.minecraft.server.World w = ((CraftWorld)target.getWorld()).getHandle();
			w.applyPhysics(target.getX(), target.getY(), target.getZ(), target.getType().getId());
			w.applyPhysics(target.getX(), target.getY()-1, target.getZ(), target.getType().getId());
			return true;
		} else {
			return false;
		}		
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		return activate(target.getBlock());
	}
}