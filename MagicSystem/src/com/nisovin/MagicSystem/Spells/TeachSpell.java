package com.nisovin.MagicSystem.Spells;

public class TeachSpell extends CommandSpell {

	private static final String SPELL_NAME = "teach";

	private String strUsage;
	private String strNoTarget;
	private String strNoSpell;
	
	public static void load(Configuration config) {
		if (config.getBoolean("spells." + SPELL_NAME + ".enabled", true)) {
			MagicSystem.spells.put(SPELL_NAME, new TeachSpell(config));
		}
	}
	
	public TeachSpell(Configuration config) {
		super(config, SPELL_NAME);
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args.length != 2) {
				// fail: missing args
				sendMessage(player, strUsage);
				return true;
			} else {
				List<Player> players = MagicSystem.plugin.getServer().matchPlayer(args[0]);
				if (players.size() != 1) {
					// fail: no player match
					sendMessage(player, strNoTarget);
					return true;
				} else {
					Spell spell = MagicSystem.spells.get(args[1]);
					if (spell == null) {
						// fail: no spell match
						sendMessage(player, strNoSpell);
						return true;
					} else {
						Spellbook spellbook = MagicSystem.getSpellbook(player);
						if (spellbook == null || spellbook.hasSpell(spell)) {
							// fail: player doesn't have spell
							sendMessage(player, strNoSpell);
							return true;
						} else {
							// yay! can learn!
							Spellbook targetSpellbook = MagicSystem.getSpellbook(players.get(0));
							if (targetSpellbook == null) {
								// fail: no spellbook for some reason
								return true;
							} else {
								targetSpellbook.addSpell(spell);
								targetSpellbook.save();
								// TODO: send messages
							}
						}
					}
				}
			}
		}
		return false;
	}

}