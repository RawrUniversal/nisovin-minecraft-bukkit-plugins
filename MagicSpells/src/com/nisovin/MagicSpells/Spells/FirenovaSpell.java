package com.nisovin.MagicSpells.Spells;

public class FirenovaSpell extends InstantSpell {

	private static final String SPELL_NAME = "firenova";

	private int fireRings;
	private int tickSpeed;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FirenovaSpell(config, spellName));
		}
	}
	
	public FirenovaSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		fireRings = config.getInt("spells." + spellName + ".fire-rings", 4);
		tickSpeed = config.getInt("spells." + spellName + ".tick-speed", 25);
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			new FireNovaAnimation(player.getLocation().getBlock());
		}
		return false;
	}

	private class FirenovaAnimation implements Runnable {
		int i;
		Block center;
		HashSet<Block> fireBlocks;
		int taskId;
		
		public FirenovaAnimation(Block center) {
			this.i = 0;
			this.center = center;
			this.fireBlocks = new HashSet<Block>();
			this.taskId = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, 0, tickSpeed);
		}
		
		public void run() {
			// remove old fire blocks
			for (Block block : fireBlocks) {
				if (block.getType() == Material.FIRE) {
					block.setType(Material.AIR);
				}
			}
			fireBlocks.clear();
			
			// set next ring on fire
			i += 1;
			int bx = center.getX();
			int y = center.getY();
			int bz = center.getZ();
			for (int x = bx - i; x <= bx + i; x++) {
				for (int z = bz - i; z <= bz + i; z++) {
					if (Math.abs(x-bx) == i || Math.abs(z-bz) == i) {
						Block b = center.getWorld().getBlockAt(x,y,z);
						if (b.getType() == Material.AIR) {
							b.setType(Material.FIRE);
							fireBlocks.add(b);
						}
					}
				}
			}
			
			// stop if done
			if (i == fireRings) {
				Bukkit.getServer().getScheduler().cancelTask(taskId);
			}
		}
	}

}