package com.nisovin.MagicSpells.Spells;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.util.config.Configuration;

import com.nisovin.MagicSpells.MagicSpells;
import com.nisovin.MagicSpells.Spell;

public class ExternalCommandSpell extends Spell {
	
	private static final String SPELL_NAME = "external";

	private boolean castWithItem;
	private boolean castByCommand;
	private String commandToExecute;
	private String commandToBlock;
	private String strCantUseCommand;
	
	public static void load(Configuration config) {
		load(config, SPELL_NAME);
	}
	
	public static void load(Configuration config, String spellName) {
		if (config.getBoolean("spells." + spellName + ".enabled", true)) {
			MagicSpells.spells.put(spellName, new ExternalCommandSpell(config, spellName));
		}
		
	}

	public ExternalCommandSpell(Configuration config, String spellName) {
		super(config, spellName);
		
		addListener(Event.Type.PLAYER_COMMAND_PREPROCESS);
		
		castWithItem = config.getBoolean("spells." + spellName + ".can-cast-with-item", true);
		castByCommand = config.getBoolean("spells." + spellName + ".can-cast-by-command", true);
		commandToExecute = config.getString("spells." + spellName + ".command-to-execute", "");
		commandToBlock = config.getString("spells." + spellName + ".command-to-block", "");
		strCantUseCommand = config.getString("spells." + spellName + ".str-cant-use-command", "&4You don't have permission to do that.");
	}

	@Override
	protected boolean castSpell(Player player, SpellCastState state, String[] args) {
		if (commandToExecute.equals("")) {
			Bukkit.getServer().getLogger().severe("MagicSpells: External command spell '" + name + "' has no command to execute.");
			return true;
		} else if (state == SpellCastState.NORMAL) {
			player.performCommand(commandToExecute);
		}
		return false;
	}
	
	@Override
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!event.getPlayer().isOp() && !commandToBlock.equals("") && event.getMessage().startsWith("/"+commandToBlock)) {
			event.setCancelled(true);
			sendMessage(event.getPlayer(), strCantUseCommand);
		}
	}

	@Override
	public boolean canCastByCommand() {
		return castByCommand;
	}

	@Override
	public boolean canCastWithItem() {
		return castWithItem;
	}

}
