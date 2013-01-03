package com.nisovin.barnyard;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class CapturedArea {

	private static Random random = new Random();
	
	private int areaX;
	private int areaZ;
	
	public CapturedArea(int areaX, int areaZ) {
		this.areaX = areaX;
		this.areaZ = areaZ;
	}
	
	public CapturedArea(int x, int y, int z) {
		this.areaX = (int)Math.floor((float)x / BarnYardBlitz.areaSize);
		this.areaZ = (int)Math.floor((float)z / BarnYardBlitz.areaSize);
	}
	
	public CapturedArea(Location location) {
		this.areaX = (int)Math.floor(location.getX() / BarnYardBlitz.areaSize);
		this.areaZ = (int)Math.floor(location.getZ() / BarnYardBlitz.areaSize);
	}
	
	public CapturedArea(String string) {
		String[] data = string.split(",");
		this.areaX = Integer.parseInt(data[0]);
		this.areaZ = Integer.parseInt(data[1]);
	}
	
	public boolean inCaptureArea(Location location) {
		int minX = areaX * BarnYardBlitz.areaSize + BarnYardBlitz.noCaptureBorder;
		int maxX = areaX * BarnYardBlitz.areaSize + BarnYardBlitz.areaSize - BarnYardBlitz.noCaptureBorder;
		int minZ = areaZ * BarnYardBlitz.areaSize + BarnYardBlitz.noCaptureBorder;
		int maxZ = areaZ * BarnYardBlitz.areaSize + BarnYardBlitz.areaSize - BarnYardBlitz.noCaptureBorder;
		int x = location.getBlockX();
		int z = location.getBlockZ();
		return minX <= x && x <= maxX && minZ <= z && z <= maxZ;
	}

	public void setAreaType(World world, int type1, byte data1, int y) {
		setAreaType(world, type1, data1, 0, (byte)0, y);
	}
	
	public void setAreaType(World world, int type1, byte data1, int type2, byte data2, int y) {
		Block b1, b2;
		/*for (int x = areaX * FarmYardRumble.areaSize; x < areaX * FarmYardRumble.areaSize + FarmYardRumble.areaSize; x++) {
			for (int z = areaZ * FarmYardRumble.areaSize; z < areaZ * FarmYardRumble.areaSize + FarmYardRumble.areaSize; z++) {
				b = world.getBlockAt(x, y, z);
				FarmYardRumble.blockQueue.add(b, type1 != 44 ? type1 : random.nextInt(2) == 0 ? 43 : 44, data1);
				b = world.getBlockAt(x, y + 1, z);
				if (b.getTypeId() != type2 || b.getData() != data2) {					
					FarmYardRumble.blockQueue.add(b, type2, data2);
				}
			}
		}*/
		int xOffset = areaX * BarnYardBlitz.areaSize;
		int zOffset = areaZ * BarnYardBlitz.areaSize;
		for (int i = 0; i < BarnYardBlitz.areaSize / 2; i++) {
			for (int x = i; x < BarnYardBlitz.areaSize - i; x++) {
				for (int z = i; z < BarnYardBlitz.areaSize - i; z++) {
					if (x == i || z == i || x == BarnYardBlitz.areaSize - i - 1 || z == BarnYardBlitz.areaSize - i - 1) {
						b1 = world.getBlockAt(x + xOffset, y, z + zOffset);
						b2 = world.getBlockAt(x + xOffset, y + 1, z + zOffset);
						if (b2.getTypeId() != 0 && type2 == 0) {
							// do top first
							if (!ignoreBlock(b1.getTypeId())) {
								if ((b2.getTypeId() != type2 || b2.getData() != data2) && !ignoreBlock(b2.getTypeId())) {
									BarnYardBlitz.blockQueue.add(b2, type2, data2);
								}
								BarnYardBlitz.blockQueue.add(b1, type1 != 44 ? type1 : random.nextInt(2) == 0 ? 43 : 44, data1);
							}
						} else {
							// do bottom first
							if (!ignoreBlock(b1.getTypeId())) {
								BarnYardBlitz.blockQueue.add(b1, type1 != 44 ? type1 : random.nextInt(2) == 0 ? 43 : 44, data1);
								if ((b2.getTypeId() != type2 || b2.getData() != data2) && !ignoreBlock(b2.getTypeId())) {
									BarnYardBlitz.blockQueue.add(b2, type2, data2);
								}
							}
						}
					}
				}					
			}
		}
	}
	
	public String getId() {
		return areaX + "," + areaZ;
	}
	
	public Location getRandomLocationInArea(World world) {
		int x = areaX * BarnYardBlitz.areaSize + 2 + random.nextInt(BarnYardBlitz.areaSize - 4);
		int z = areaZ * BarnYardBlitz.areaSize + 2 + random.nextInt(BarnYardBlitz.areaSize - 4);
		int y = world.getHighestBlockYAt(x, z) + 1;
		return new Location(world, x, y, z);
	}
	
	public Location getCenter(World world) {
		int x = areaX * BarnYardBlitz.areaSize + (BarnYardBlitz.areaSize / 2);
		int z = areaZ * BarnYardBlitz.areaSize + (BarnYardBlitz.areaSize / 2);
		int y = world.getHighestBlockYAt(x, z) + 1;
		return new Location(world, x, y, z);
	}
	
	private boolean ignoreBlock(int typeId) {
		return Arrays.binarySearch(BarnYardBlitz.ignoreBlocks, typeId) >= 0;
	}
	
	@Override
	public int hashCode() {
		return areaX * 1000 + areaZ;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else if (o == this) {
			return true;
		} else if (!(o instanceof CapturedArea)) {
			return false;
		} else {
			CapturedArea a = (CapturedArea)o;
			return a.areaX == this.areaX && a.areaZ == this.areaZ;
		}
	}
	
	public static void main(String[] args) {
		System.out.println((int)Math.floor(-32F / 64));
		System.out.println((int)Math.floor(32F / 64));
		System.out.println((int)Math.floor(-32F / 64));
		System.out.println((int)Math.floor(32F / 64));
	}
	
}
