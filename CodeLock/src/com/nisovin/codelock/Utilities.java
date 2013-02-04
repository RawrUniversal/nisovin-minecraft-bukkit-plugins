package com.nisovin.codelock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Attachable;
import org.bukkit.material.TrapDoor;

public class Utilities {
	
	static String getLocStr(Block block) {
		return getLocStr(block, 0, 0, 0);
	}
	
	static String getLocStr(Block block, int offsetX, int offsetY, int offsetZ) {
		return block.getWorld().getName() + "," + (block.getX() + offsetX) + "," + (block.getY() + offsetY) + "," + (block.getZ() + offsetZ);
	}
	
	static boolean isDoorClosed(Block block) {
		if (block.getType() == Material.TRAP_DOOR) {
			TrapDoor trapdoor = (TrapDoor)block.getState().getData();
			return !trapdoor.isOpen();
		} else {
			byte data = block.getData();
			if ((data & 0x8) == 0x8) {
				block = block.getRelative(BlockFace.DOWN);
				data = block.getData();
			}
			return ((data & 0x4) == 0);
		}
	}
	
	static void openDoor(Block block) {
		if (block.getType() == Material.TRAP_DOOR) {
			BlockState state = block.getState();
			TrapDoor trapdoor = (TrapDoor)state.getData();
			trapdoor.setOpen(true);
			state.update();
		} else {
			byte data = block.getData();
			if ((data & 0x8) == 0x8) {
				block = block.getRelative(BlockFace.DOWN);
				data = block.getData();
			}
			if (isDoorClosed(block)) {
				data = (byte) (data | 0x4);
				block.setData(data, true);
				block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
			}
		}
	}
	
	static void closeDoor(Block block) {
		if (block.getType() == Material.TRAP_DOOR) {
			BlockState state = block.getState();
			TrapDoor trapdoor = (TrapDoor)state.getData();
			trapdoor.setOpen(false);
			state.update();
		} else {
			byte data = block.getData();
			if ((data & 0x8) == 0x8) {
				block = block.getRelative(BlockFace.DOWN);
				data = block.getData();
			}
			if (!isDoorClosed(block)) {
				data = (byte) (data & 0xb);
				block.setData(data, true);
				block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
			}
		}
	}
	
	static void redstoneUpdate(Block block) {
		try {
			Block b = block.getRelative(((Attachable)block.getState().getData()).getAttachedFace());
			Field worldField = b.getWorld().getClass().getDeclaredField("world");
			worldField.setAccessible(true);
			Object world = worldField.get(b.getWorld());
			Method applyPhysicsMethod = world.getClass().getSuperclass().getDeclaredMethod("applyPhysics", int.class, int.class, int.class, int.class);
			applyPhysicsMethod.setAccessible(true);
			applyPhysicsMethod.invoke(world, b.getX(), b.getY(), b.getZ(), b.getTypeId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
