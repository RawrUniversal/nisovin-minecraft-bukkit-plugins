package com.nisovin.worldloader;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyWorldGen extends ChunkGenerator {

	@Override
	public byte[] generate(World world, Random rand, int cx, int cz) {
		byte[] result = new byte[32768];
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < 128; y++) {
					result[(x * 16 + z) * 128 + y] = 0;
				}
			}
		}
		return result;
	}
	
}
