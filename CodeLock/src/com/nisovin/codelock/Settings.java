package com.nisovin.codelock;

import org.bukkit.Material;

public class Settings {
	
	static int lockInventorySize = 27;
	static String lockTitle = "Code Entry";
	
	static Material[] buttons = new Material[] {
		Material.APPLE, Material.BOOK, Material.COAL,
		Material.DIAMOND, Material.EGG, Material.FURNACE,
		Material.GLASS, Material.STONE_HOE, Material.IRON_INGOT
	};
	static int[] buttonPositions = new int[] {
		3, 4, 5,
		12, 13, 14, 
		21, 22, 23
	};
	static char[] letterCodes = new char[] {
		'A', 'B', 'C',
		'D', 'E', 'F',
		'G', 'H', 'I'
	};
	
	static int autoDoorClose = 100;
	static boolean checkBuildPerms = true;
	
	static String strLocked = "Locked with code: ";
	static String strRemoved = "Removed lock.";
	
}
