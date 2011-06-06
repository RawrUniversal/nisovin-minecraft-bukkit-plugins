public class FireballSpell extends InstantSpell {

	private static final String SPELL_NAME = "fireball";
	
	private String strNoTarget;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new FireballSpell(config, spellName));
		}
	}
	
	public FireballSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		strNoTarget = config.getString("spells." + spellName + ".str-no-target", "You cannot throw a fireball there.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Block target = player.getTargetBlock(null, range);
			if (target == null) {
				// fail -- no target
				sendMessage(player, strNoTarget);
				return true;
			} else {
				Location blockLoc = target.getLocation();
				blockLoc.setX(blockLoc.getX()+.5);
				blockLoc.setY(blockLoc.getY()+.5);
				blockLoc.setZ(blockLoc.getZ()+.5);
				Vector path = blockLoc.toVector().substract(player.getEyeLocation().toVector());				
				
				EntityFireball fireball = new EntityFireball(((CraftWorld)player.getWorld()).getHandle(), ((CraftPlayer)player).getHandle(), path.getX(), path.getY(), path.getZ());
				((CraftWorld)player.getWorld()).getHandle().a(fireball);
			}
		}
		return false;
	}
	
	
}