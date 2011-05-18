package com.nisovin.MagicSpells.Spells;

public class TelekinesisSpell extends InstantSpell {

	private static final String SPELL_NAME = "telekinesis";

	private String strNoTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new TelekinesisSpell(config, spellName));
		}
	}
	
	public VolleySpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You must target a switch or button.");
	}
	
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range>0?range:100);
			if (target == null) {
				// fail
				sendMessage(player, strNoTarget);
				return true;
			} else if (target.getType() == Material.LEVER || target.getType() == Material.BUTTON) {
				target.setData(target.getData() ^ 0x8);
			} else if (target.getType() == Material.WOOD_PLATE || target.getType() == Material.STONE_PLATE) {
				target.setData(target.getData() ^ 0x1);				
			} else {
				// fail
				sendMessage(player, strNoTarget);
				return true;
			}
		}
		return false;
	}
}