public class SpellbookSpell extends CommandSpell {
	
	private static final String SPELL_NAME = "spellbook";
	
	private int defaultUses;
	private String strUsage;
	private String strNoSpell;
	private String strNoTarget;
	private String strHasSpellbook;
	private String strCantDestroy;
	private String strLearnError;
	private String strAlreadyKnown;
	private String strLearned;
	
	private ArrayList<Location> bookLocations;
	private ArrayList<String> bookSpells;
	private ArrayList<Integer> bookUses;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new SpellbookSpell(config, spellName));
		}
	}
	
	public SpellbookSpell(Configuration config, String spellName) {
		bookLocations = new ArrayList<Block>();
		bookSpells = new ArrayList<String>();
		bookUses = new ArrayList<Integer>();
		
		addListener(Event.Type.PLAYER_INTERACT);
		addListener(Event.Type.BLOCK_BREAK);
		
		loadSpellbooks();
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (state == SpellCastState.NORMAL) {
			if (args.length < 1 || args.length > 2 || (args.length == 2 && !args[1].matches("^[0-9]+$"))) {
				// fail: show usage string
				sendMessage(player, strUsage);
			} else {
				Spell spell = MagicSpells.getSpellbook(player).getSpellByName(args[0]);
				if (spell == null) {
					// fail: no such spell
					sendMessage(player, strNoSpell);
				} else {
					Block target = player.getTargetedBlock(null, 10);
					if (target == null || target.getType() != Material.BOOKCASE) {
						// fail: must target a bookcase
						sendMessage(player, strNoTarget);
					} else if (bookLocations.contains(target.getLocation()) {
						// fail: already a spellbook there
						sendMessage(player, strHasSpellbook);
					} else {
						// create spellbook
						bookLocations.add(target.getLocation());
						bookSpells.add(spell.getInternalName());
						if (args.length == 1) {
							bookUses.add(defaultUses);
						} else {
							bookUses.add(Integer.parseInt(args[1]));
						}
						saveSpellbooks();
						sendMessage(player, formatMessage(strCastSelf, "%s", spell.getName()));
						startCooldown(player);
						removeReagents(player);
					}
				}
			}
		}
		return true;
	}
	
	private void removeSpellbook(int index) {
		bookLocations.remove(index);
		bookSpells.remove(index);
		bookUses.remove(index);
		saveSpellbooks();
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.hasBlock() && event.getClickedBlock().getType() == Material.BOOKCASE && bookLocations.contains(event.getClickedBlock().getLocation())) {
			int i = bookLocations.indexOf(event.getClickedBlock().getLocation());
			Spellbook spellbook = MagicSpells.spellbooks.get(player.getName());
			Spell spell = MagicSpells.spells.get(bookSpells.get(i));
			if (spellbook == null || spell == null) {
				// fail: something's wrong
				sendMessage(player, strLearnError);
			} else if (spellbook.hasSpell(spell)) {
				// fail: already known
				sendMessage(player, strAlreadyKnown);
			} else {
				// teach the spell
				spellbook.addSpell(spell);
				spellbook.save();
				sendMessage(player, formatMessage(strLearned, "%s", spell.getName()));
				int uses = bookUses.get(i);
				if (uses > 0) {
					uses--;
					if (uses == 0) {
						// remove the spellbook
						removeSpellbook(i);
					} else {
						bookUses.set(i, uses);
					}
				}
			}
		}
	}
	
	@Override
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.BOOKCASE && bookLocations.contains(event.getBlock().getLocation())) {
			if (event.getPlayer().isOp()) {
				// remove the bookcase
				int i = bookLocations.indexOf(event.getBlock().getLocation());
				removeBookcase(i);
			} else {
				// cancel it
				event.setCancelled(true);
			}
		}
	}
	
	private void loadSpellbooks() {
	}
	
	private void saveSpellbooks() {
	}
	
}