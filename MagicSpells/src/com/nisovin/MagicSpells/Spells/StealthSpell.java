package com.nisovin.MagicSpells.Spells;

public class StealthSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "stealth";
	
	private HashSet<String> stealthy;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new StealthSpell(config, spellName));
		}		
	}
	
	public StealthSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		addListener(Event.Type.ENTITY_TARGET);
		
		stealthy = new HashSet<String>();
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (stealthy.contains(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			stealty.add(player.getName());
			startSpellDuration();
		}
		return false;
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (!event.isCancelled() && stealty.size() > 0 && event.getTarget() instanceof Player) {
			Player player = (Player)entity.getTarget();
			if (stealty.contains(player.getName())) {
				if (isExpired(player)) {
					turnOff(player);
				} else {
					addUse(player);
					boolean ok = chargeUseCost(player);
					if (ok) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@Override
	protected void turnOff(Player player) {
		super.turnOff(player);
		stealty.remove(player.getName());
		sendMessage(player, strFade);
	}
	
	@Override
	protected void turnOff() {
		stealthy.clear();
	}
	
}