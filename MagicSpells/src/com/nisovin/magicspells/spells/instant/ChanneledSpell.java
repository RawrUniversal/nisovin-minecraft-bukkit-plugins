package com.nisovin.magicspells.spells.instant;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.MagicConfig;

public class ChanneledSpell extends InstantSpell {
	
	private int channelTime;
	private int reqParticipants;
	private boolean needSpellToParticipate;
	private boolean showProgressOnExpBar;
	private boolean chargeReagentsUpFront;
	private Spell spell;
	private String theSpellName;
	private int tickInterval;
	private int effectInterval;
	private String strChannelJoined;
	private String strChannelSuccess;
	private String strChannelInterrupted;
	private String strChannelFailed;
	
	private HashMap<Player, ActiveSpellChannel> activeChannels;
	
	public ChanneledSpell(MagicConfig config, String spellName) {
		super(config, spellName);
		
		channelTime = getConfigInt("channel-time", 200);
		reqParticipants = getConfigInt("req-participants", 1);
		needSpellToParticipate = getConfigBoolean("need-spell-to-participate", false);
		showProgressOnExpBar = getConfigBoolean("show-progress-on-exp-bar", true);
		chargeReagentsUpFront = getConfigBoolean("charge-reagents-up-front", true);
		theSpellName = getConfigString("spell", "");
		tickInterval = getConfigInt("tick-interval", 5);
		effectInterval = getConfigInt("effect-interval", 20);
		strChannelJoined = getConfigString("str-channel-joined", null);
		strChannelSuccess = getConfigString("str-channel-success", null);
		strChannelInterrupted = getConfigString("str-channel-interrupted", null);
		strChannelFailed = getConfigString("str-channel-failed", null);
		
		activeChannels = new HashMap<Player, ActiveSpellChannel>();
	}
	
	@Override
	public void initialize() {
		super.initialize();
		spell = MagicSpells.getSpellByInternalName(theSpellName);
		if (spell == null) {
			MagicSpells.error("ChanneledSpell '" + internalName + "' does not have a spell defined (" + theSpellName + ")!");
		}
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (activeChannels.containsKey(player)) {
			ActiveSpellChannel channel = activeChannels.remove(player);
			channel.stop(strChannelInterrupted);
		}
		if (state == SpellCastState.NORMAL) {
			activeChannels.put(player, new ActiveSpellChannel(player, power, args));
			if (!chargeReagentsUpFront) {
				return PostCastAction.NO_REAGENTS;
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlayerInteract(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Player) {
			ActiveSpellChannel channel = activeChannels.get((Player)event.getRightClicked());
			if (channel != null) {
				if (!needSpellToParticipate || hasThisSpell(event.getPlayer())) {
					channel.addChanneler(event.getPlayer());
					sendMessage(event.getPlayer(), strChannelJoined);
				}
			}
		}
	}
	
	private boolean hasThisSpell(Player player) {
		return MagicSpells.getSpellbook(player).hasSpell(this);
	}
	
	public class ActiveSpellChannel implements Runnable {
		
		private Player caster;
		private HashMap<Player, Location> channelers = new HashMap<Player, Location>();
		private float power;
		private String[] args;
		private int duration = 0;
		private int taskId;
		
		public ActiveSpellChannel(Player caster, float power, String[] args) {
			this.power = power;
			this.args = args;
			this.caster = caster;
			this.channelers.put(caster, caster.getLocation().clone());
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(MagicSpells.plugin, this, tickInterval, tickInterval);
			playSpellEffects(EffectPosition.CASTER, caster);
		}
		
		public void addChanneler(Player player) {
			if (!channelers.containsKey(player)) {
				channelers.put(player, player.getLocation().clone());
				playSpellEffects(EffectPosition.CASTER, player);
			}
		}
		
		public void removeChanneler(Player player) {
			channelers.remove(player);
		}
		
		public boolean isChanneler(Player player) {
			return channelers.containsKey(player);
		}
		
		@Override
		public void run() {
			duration += tickInterval;

			int count = channelers.size();
			boolean interrupted = false;
			for (Player player : channelers.keySet()) {
				// check for movement
				Location oldloc = channelers.get(player);
				Location newloc = player.getLocation();
				if (Math.abs(oldloc.getX() - newloc.getX()) > .01 || Math.abs(oldloc.getY() - newloc.getY()) > .01 || Math.abs(oldloc.getZ() - newloc.getZ()) > .01) {
					interrupted = true;
					break;
				}
				// send exp bar update
				if (showProgressOnExpBar) {
					MagicSpells.getVolatileCodeHandler().setExperienceBar(player, count, (float)duration / (float)channelTime);
				}
				// spell effect
				if (duration % effectInterval == 0) {
					playSpellEffects(EffectPosition.CASTER, player);
				}
			}
			if (interrupted) {
				stop(strChannelInterrupted);
			}
			
			if (duration >= channelTime) {
				// channel is done
				if (count >= reqParticipants && !caster.isDead() && caster.isOnline()) {
					if (chargeReagentsUpFront || hasReagents(caster)) {
						stop(strChannelSuccess);
						PostCastAction action = spell.castSpell(caster, SpellCastState.NORMAL, power, args);
						if (!chargeReagentsUpFront && (action == PostCastAction.HANDLE_NORMALLY || action == PostCastAction.NO_COOLDOWN || action == PostCastAction.NO_MESSAGES || action == PostCastAction.REAGENTS_ONLY)) {
							removeReagents(caster);
						}
					} else {
						stop(strChannelFailed);
					}
				} else {
					stop(strChannelFailed);
				}
			}
		}
		
		public void stop(String message) {
			for (Player player : channelers.keySet()) {
				sendMessage(player, message);
				MagicSpells.getVolatileCodeHandler().setExperienceBar(player, player.getLevel(), player.getExp());
			}
			Bukkit.getScheduler().cancelTask(taskId);
			activeChannels.remove(caster);
		}
		
	}

}
