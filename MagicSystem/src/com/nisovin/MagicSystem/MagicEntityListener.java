public class MagicEntityListener extends EntityListener {
	
	public MagicEntityListener(MagicSystem plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Type.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_TARGET, this, Event.Type.Normal, plugin);
	}
	
	public onEntityDamage(EntityDamageEvent event) {
		HashSet<Spell> spells = MagicSystem.listeners.get(Event.Type.ENTITY_DAMAGE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityDamage(event);
			}
		}
	}
	
	public onEntityTarget(EntityTargetEvent event) {
		HashSet<Spell> spells = MagicSystem.listeners.get(Event.Type.ENTITY_TARGET);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onEntityTarget(event);
			}
		}		
	}
	
}