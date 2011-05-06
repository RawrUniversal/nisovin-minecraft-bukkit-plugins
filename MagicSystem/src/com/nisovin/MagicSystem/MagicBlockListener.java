public class MagicBlockListener extends BlockListener {
	
	public MagicBlockListener(MagicSystem plugin) {
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_BREAK, this, Event.Type.Normal, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.BLOCK_PLACE, this, Event.Type.Normal, plugin);
	}
	
	public void onBlockBreak(BlockBreakEvent event) {
		HashSet<Spell> spells = MagicSystem.listeners.get(Event.Type.BLOCK_BREAK);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onBlockBreak(event);
			}
		}
	}
	
	public void onBlockPlace(BlockPlaceEvent event) {
		HashSet<Spell> spells = MagicSystem.listeners.get(Event.Type.BLOCK_PLACE);
		if (spells != null) {
			for (Spell spell : spells) {
				spell.onBlockPlace(event);
			}
		}
	}	
	
}