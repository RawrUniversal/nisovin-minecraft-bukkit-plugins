public class CombustSpell extends WandSpell {

	private static final String SPELL_NAME = "combust";
	
	private boolean targetPlayers;
	private int fireTicks;
	private String strNoTarget;
	
	public static void load(Configuration config) {
		if (config.getBoolean("spells." + SPELL_NAME + ".enabled", true)) {
			MagicSystem.spells.put(SPELL_NAME, new CombustSpell(config));
		}
	}
	
	public CombustSpell(Configuration config) {
		super(config, SPELL_NAME);
		
		targetPlayers = config.getBoolean("spells." + SPELL_NAME + ".target-players", false);
		fireTicks = config.getInt("spells." + SPELL_NAME + ".fire-ticks", 100);
		strNoTarget = config.getString("spells." + SPELL_NAME + ".str-no-target", "No target to combust.");
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Entity target = getTargetedEntity(player, range, targetPlayers);
			if (target == null) {
				sendMessage(player, strNoTarget);
				return true;
			} else {
				target.setFireTicks(fireTicks);
			}
		}
		return false;
	}
}