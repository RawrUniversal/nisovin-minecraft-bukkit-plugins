package com.nisovin.MagicSpells.Spells;

public class BuildSpell extends InstantSpell {
	
	private static final String SPELL_NAME = "build";
	
	private int slot;
	private int[] allowedTypes;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new BuildSpell(config, spellName));
		}
	}
	
	public BuildSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		slot = config.getInt("spells." + spellName + ".slot", 8);
		String[] allowed = config.getString("spells." + spellName + ".allowed-types", "1,2,3,4,5,6").split(",");
		allowedTypes = new int[allowed.length];
		for (int i = 0; i < allowed.length; i++) {
			allowedTypes[i] = Integer.parseInt(allowed[i]);
		}
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// get mat
			Material mat = null;
			ItemStack item = player.getInventory().getItem(slot);
			if (item == null || !isAllowed(item.getType())) {
				// fail
				return true;
			} else {
				mat = item.getType();
			}
			
			// get target
			List<Block> lastBlocks = player.getLastTwoTargetBlocks(null, range);
			if (lastBlocks.get(1).getType() == Material.AIR) {
				// fail
				return true;
			} else {
				Block b = lastBlocks.get(0);
				b.setType(mat);
				item.setAmount(item.getAmount()-1);
				player.getInventory().setItem(slot, item);
			}
		}
		return false;
	}
	
	private boolean isAllowed(Material mat) {
		for (int i = 0; i < allowedTypes.length; i++) {
			if (allowedTypes[i] == mat.getId()) {
				return true;
			}
		}
		return false;
	}
}