package com.nisovin.MagicSpells.Spells;

public class MinionSpell extends BuffSpell {
	
	private static final String SPELL_NAME = "minion";
	
	private CreatureType[] creatureTypes;
	private int[] chances;
	private boolean targetPlayers;
	
	private HashMap<String,LivingEntity> minions;
	private HashMap<String,LivingEntity> targets;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new MinionSpell(config, spellName));
		}
	}
	
	public MinionSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		addListener(Event.Type.ENTITY_TARGET);
		addListener(Event.Type.ENTITY_DAMAGE);
		
		List<String> c = config.getStringList("spells." + spellName + ".mob-chances");
		creatureTypes = new creatureTypes[c.size()];
		chances = new int[c.size()];
		for (int i = 0; i < c.size(); i++) {
			String[] data = c.get(i).split(" ");
			CreatureType creatureType = CreatureType.fromName(data[0].toUpperCase());
			int chance = 0;
			if (creatureType != null) {
				try {
					chance = Integer.parseInt(data[1]);
				} catch (NumberFormatException e) {
				}
			}
			creatureTypes[i] = creatureType;
			chances[i] = chance;
		}
		targetPlayers = config.getBoolean("spells." + spellName + ".target-players", false);
		
		minions = new HashMap<String,LivingEntity>();
		targets = new HashMap<String,LivingEntity>();
	}
	
	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (stealthy.contains(player.getName())) {
			turnOff(player);
			return true;
		} else if (state == SpellCastState.NORMAL) {
			Random random = new Random();
			CreatureType creatureType = null;
			int num = random.nextInt(100);
			int n = 0;
			for (int i = 0; i < creatureTypes.length; i++) {
				if (num < chances[i] + n) {
					creatureType = creatureTypes[i];
					break;
				} else {
					n += chances[i];
				}
			}
			if (creatureType != null) {
				// get spawn location
				Location loc = null;
				loc = player.getLocation();
				loc.setX(loc.getX()-1);
				
				// spawn creature
				LivingEntity minion = player.getWorld().spawnCreature(loc, creatureType);
				minions.put(player.getName(), minion.getEntityId());
				targets.put(player.getName(), null);
			} else {
				// fail -- no creature found
			}
		}
		return false;
	}
	
	@Override
	public void onEntityTarget(EntityTargetEvent event) {
		if (!event.isCancelled() && minions.size() > 0 && event.getTarget() instanceof Player) {			
			Player player = (Player)entity.getTarget();
			LivingEntity minion = minions.get(player.getName());
			if (minion != null && minion.getEntityId() == event.getEntity().getEntityId()) {
				LivingEntity target = targets.get(player.getName();
				if (target != null) {
					event.setTarget(targets.get(player.getName());
				} else {
					EntityCreature entity = ((CraftCreature)minion).getHandle();
					entity.pathEntity = entity.world.findPath(entity, ((CraftPlayer)player).getHandle(), 16.0F);
				}
			} else if (!targetPlayers && minions.contains(event.getEntity().getEntityId())) {
				event.setCancelled(true);
			}
		}
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (!event.isCancelled() && event instanceof EntityDamageByEntityEvent && event.getEntity() instanceof LivingEntity) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent)event;
			if (evt.getDamager() instanceof Player) {
				Player p = (Player)evt.getDamager();
				if (minions.containsKey(p.getName())) {
					LivingEntity target = (LivingEntity)event.getEntity();
					((Creature)minions.get(p.getName())).setTarget(target);
					targets.put(p.getName(), target);
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