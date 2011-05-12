public class HealthBars extends JavaPlugin {

	public static HealthBars instance;	
	private static HealthBarUpdater updater;
	
	private int taskId;
	private HashMap<String,HashSet<HealthBar>>() healthbars;
	
	public void onEnable() {
		this.instance = this;
		this.healthbars = HashMap<String,HashSet<HealthBar>>();
		
		new HealthBarsPlayerListener(this);
	}
	
	public void addHealthBar(Player owner, Player target, int slot, Material tool) {
		HealthBar bar = new HealthBar(owner, target, slot, tool);
		HashSet<HealthBar> bars = healthbars.get(target.getName());
		if (bars == null) {
			bars = new HashSet<HealthBar>();
			healthbars.put(target.getName(), bars);
		}
		bars.add(bar);
	}
	
	public void startUpdater() {
		taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, updater, 20, 20);
	}
	
	public void stopUpdater() {
		if (updater != null) {
			getServer().getScheduler().cancelTask(taskId);
			updater = null;
		}
	}
	
	public static void queueHealthUpdate(Player player) {
		if (instance.healthbars.containsKey(player.getName())) {
			if (updater == null) {
				updater = new HealthBarUpdater();
				instance.startUpdater();
			}
			updater.update(player);
		}
	}
	
	public HashSet<HealthBar> getHealthBars(String player) {
		if (healthbars == null) return null;
		return healthbars.get(player);
	}
	
	public void onDisable() {
		
	}


}

public class HealthBarPlayerListener extends PlayerListener {
	
	private HealthBars plugin;
	
	public HealthBarPlayerListener(HealthBars plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this, Event.Priority.Monitor, plugin);
		plugin.getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!event.isCancelled()) {
			Player player = event.getPlayer();
			Entity target = event.getRightClicked();
			if (target instanceof Player && player.getItemInHand().getType().toString().startsWith("WOOD_")) {
				plugin.addHealthBar(player, (Player)target, player.getInventory().getHeldItemSlot(), player.getItemInHand().getType());
			}
		}
	}
	
	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!event.isCancelled()) {
			if (event.hasItem() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && isFood(event.getPlayer().getItemInHand().getType())) {
				HealthBars.queueHealthUpdate(event.getPlayer());
			} else if (event.hasBlock() && event.getBlockClicked().getType() == Material.CAKE) {
				HealthBars.queueHealthUpdate(event.getPlayer());
			}
		}
	}
	
	private boolean isFood(Material mat) {
		return mat == Material.GOLDEN_APPLE || 
			mat == Material.MUSHROOM_STEW || 
			mat == Material.COOKED_PORKCHOP ||
			mat == Material.BREAD ||
			mat == Material.COOKED_FISH ||
			mat == Material.APPLE ||
			mat == Material.RAW_PORKCHOP ||
			mat == Material.RAW_FISH ||
			mat == Material.COOKIE;
	}
	
}

public class HealthBarsEntityListener extends EntityListener {
	
	private HealthBars plugin;
	
	public HealthBarsEntityListener(HealthBars plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, this, Event.Priority.Monitor, plugin);
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player && !event.isCancelled() && event.getDamage() > 0) {
			HealthBars.queueHealthUpdate((Player)event.getEntity());
		}
	}
	
}


public class HealthBarUpdater implements Runnable {
	private HealthBars plugin;
	private HashSet<String> toUpdate;
	private int counter = 0;
	
	public HealthBarUpdater(HealthBars plugin) {
		this.plugin = plugin;
		toUpdate = new HashSet<String>();
	}
	
	public void update(Player player) {
		toUpdate.add(player.getName());
	}
	
	public void run() {
		if (toUpdate.size() > 0) {
			for (String player : toUpdate) {
				HashSet<HealthBar> bars = plugin.getHealthBarsFor(player);
				for (HealthBar bar : bars) {
					bar.sendUpdate();
				}
			}
			toUpdate.clear();
			counter = 0;
		} else {
			if (++counter > 60) {
				// no events for a while
				HealthBars.instance.stopUpdater();
			}
		}
	}
	
}

public class HealthBar {
	String owner;
	String target;
	private int slot;
	private Material tool;
	private int maxDurability;
	
	public HealthBar(Player owner, Player target, int slot, Material tool) {
		this.owner = owner.getName();
		this.target = target.getName();
		this.slot = slot;
		this.tool = tool;
		if (tool.toString().startsWith("WOOD_")) {
			maxDurability = 60;
		} else {
			throw new Exception("Invalid tool.");
		}
	}
	
	public void sendUpdate() {
		Player owner = HealthBars.instance.getServer().getPlayer(this.owner);
		Player target = HealthBars.instance.getServer().getPlayer(this.target);
		if (owner != null && target != null) {
			ItemStack item = owner.getInventory().getItem(slot);
			if (item.getType() == tool) {
				short dur = (maxDurability * target.getHealth()) / target.getMaxHealth();
				if (dur == maxDurability) {
					dur = maxDurability-1;
				} else if (dur == 0) {
					dur = 1;
				}
				item.setDurability(dur);
				owner.getInventory().setItem(slot, item);
			}
		}
	}
}