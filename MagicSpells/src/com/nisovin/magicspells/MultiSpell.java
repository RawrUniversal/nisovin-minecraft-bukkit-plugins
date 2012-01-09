package com.nisovin.magicspells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.util.CastItem;
import com.nisovin.magicspells.util.MagicConfig;

public final class MultiSpell extends Spell {

	private boolean castWithItem;
	private boolean castByCommand;
	private boolean checkIndividualCooldowns;
	
	private ArrayList<Spell> spells;
	
	public MultiSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		// get these values from the multispells section instead of the spells section
		this.name = config.getString("multispells." + spellName + ".name", spellName);
		this.description = config.getString("multispells." + spellName + ".description", "");
		this.castItem = new CastItem(config.getString("multispells." + spellName + ".cast-item", "280"));
		this.broadcastRange = config.getInt("multispells." + spellName + ".broadcast-range", MagicSpells.broadcastRange);

		List<String> costList = config.getStringList("spells." + spellName + ".cost", null);
		if (costList != null && costList.size() > 0) {
			cost = new ItemStack [costList.size()];
			String[] data, subdata;
			for (int i = 0; i < costList.size(); i++) {
				String costVal = costList.get(i);
				
				// validate cost data
				if (!costVal.matches("^([1-9][0-9]*(:[1-9][0-9]*)?|mana|health|hunger|experience|levels) [1-9][0-9]*$")) {
					MagicSpells.error(Level.WARNING, "Failed to process cost value for " + spellName + " spell: " + costVal);
					continue;
				}
				
				// parse cost data
				data = costVal.split(" ");
				if (data[0].equalsIgnoreCase("health")) {
					healthCost = Integer.parseInt(data[1]);
				} else if (data[0].equalsIgnoreCase("mana")) {
					manaCost = Integer.parseInt(data[1]);
				} else if (data[0].equalsIgnoreCase("hunger")) {
					hungerCost = Integer.parseInt(data[1]);
				} else if (data[0].equalsIgnoreCase("experience")) {
					experienceCost = Integer.parseInt(data[1]);
				} else if (data[0].equalsIgnoreCase("levels")) {
					levelsCost = Integer.parseInt(data[1]);
				} else if (data[0].contains(":")) {
					subdata = data[0].split(":");
					cost[i] = new ItemStack(Integer.parseInt(subdata[0]), Integer.parseInt(data[1]), Short.parseShort(subdata[1]));
				} else {
					cost[i] = new ItemStack(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
				}
			}
		} else {
			cost = null;
		}
		
		this.cooldown = config.getInt("multispells." + spellName + ".cooldown", 0);
		List<String> cooldowns = config.getStringList("multispells." + spellName + ".shared-cooldowns", null);
		if (cooldowns != null) {
			this.sharedCooldowns = new HashMap<Spell,Integer>();
			for (String s : cooldowns) {
				String[] data = s.split(" ");
				Spell spell = MagicSpells.getSpellByInternalName(data[0]);
				int cd = Integer.parseInt(data[1]);
				if (spell != null) {
					this.sharedCooldowns.put(spell, cd);
				}
			}
		}
		this.ignoreGlobalCooldown = config.getBoolean("multispells." + spellName + ".ignore-global-cooldown", false);
		
		this.strCost = config.getString("multispells." + spellName + ".str-cost", null);
		this.strCastSelf = config.getString("multispells." + spellName + ".str-cast-self", null);
		this.strCastOthers = config.getString("multispells." + spellName + ".str-cast-others", null);
		this.strOnCooldown = config.getString("multispells." + spellName + ".str-on-cooldown", MagicSpells.strOnCooldown);
		this.strMissingReagents = config.getString("multispells." + spellName + ".str-missing-reagents", MagicSpells.strMissingReagents);
		this.strCantCast = config.getString("multispells." + spellName + ".str-cant-cast", MagicSpells.strCantCast);
		
		castWithItem = config.getBoolean("multispells." + spellName + ".can-cast-with-item", true);
		castByCommand = config.getBoolean("multispells." + spellName + ".can-cast-by-command", true);
		checkIndividualCooldowns = config.getBoolean("multispells." + spellName + ".check-individual-cooldowns", false);

		spells = new ArrayList<Spell>();
		List<String> spellList = config.getStringList("multispells." + spellName + ".spells", null);
		if (spellList != null) {
			for (String s : spellList) {
				Spell spell = MagicSpells.getSpellByInternalName(s);
				if (spell != null) {
					spells.add(spell);
				} else {
					Bukkit.getServer().getLogger().severe("MagicSpells: no such spell '" + s + "' for multi-spell '" + spellName + "'");
				}
			}
		}
	}

	@Override
	protected PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			// check cooldowns
			if (checkIndividualCooldowns) {
				for (Spell spell : spells) {
					if (spell.onCooldown(player)) {
						// a spell is on cooldown
						sendMessage(player, MagicSpells.strOnCooldown);
						return PostCastAction.ALREADY_HANDLED;
					}
				}
			}
			
			for (Spell spell : spells) {
				spell.castSpell(player, SpellCastState.NORMAL, power, null);
			}
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean canCastWithItem() {
		return castWithItem;
	}

	@Override
	public boolean canCastByCommand() {
		return castByCommand;
	}

}
