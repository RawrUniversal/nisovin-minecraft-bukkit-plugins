package com.nisovin.MagicSpells.Spells;

public class WallSpell extends InstantSpell {

	private static final String SPELL_NAME = "wall";

	private int wallWidth;
	private int wallHeight;
	private Material wallType;
	private int wallDuration;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new WallSpell(config, spellName));
		}
	}
	
	public VolleySpell(Configuration config, String spellName) {
		super(config, spellName);
		
		wallWidth = config.getInt("spells." + spellName + ".wall-width", 5);
		wallHeight = config.getInt("spells." + spellName + ".wall-height", 3);
		wallType = Material.getMaterial(config.getInt("spells." + spellName + ".wall-type", Material.BRICK.getId()));
		wallDuration = config.getInt("spells." + spellName + ".wall-duration", 15);
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, 3);
			if (target == null || target.getType() != Material.AIR) {
				// fail
			} else {
				TemporaryBlockSet blockSet = new TemporaryBlockSet(Material.AIR, wallType);
				Location loc = target.getLocation();
				Vector dir = player.getLocation().getDirection();
				if (dir.getX() > dir.getZ()) {
					for (int z = loc.getZ() - (wallWidth/2); z <= loc.getZ() + (wallWidth/2); z++) {
						for (int y = loc.getY() - 1; y <= loc.getY() + wallHeight - 1; y++) {
							blockSet.add(player.getWorld().getBlockAt(target.getX(), y, z));
						}
					}
				} else {
					for (int x = loc.getX() - (wallWidth/2); x <= loc.getX() + (wallWidth/2); x++) {
						for (int y = loc.getY() - 1; y <= loc.getY() + wallHeight - 1; y++) {
							blockSet.add(player.getWorld().getBlockAt(x, y, target.getZ()));
						}
					}
				}
				blockSet.removeAfter(wallDuration);
			}
		}
	}
}