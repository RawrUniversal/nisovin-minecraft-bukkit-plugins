package com.nisovin.magicspells;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.mana.ManaChangeReason;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.Util;

public class CastCommand implements CommandExecutor, TabCompleter {

	MagicSpells plugin;
	boolean enableTabComplete;
	
	public CastCommand(MagicSpells plugin, boolean enableTabComplete) {
		this.plugin = plugin;
		this.enableTabComplete = enableTabComplete;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String [] args) {
		try {
			if (command.getName().equalsIgnoreCase("magicspellcast")) {
				args = Util.splitParams(args);
				if (args == null || args.length == 0) {
					if (sender instanceof Player) {
						MagicSpells.sendMessage((Player)sender, MagicSpells.strCastUsage);
					} else {
						sender.sendMessage(MagicSpells.textColor + MagicSpells.strCastUsage);
					}
				} else if (sender.isOp() && args[0].equals("forcecast") && args.length >= 3) {
					if (args[1].matches("^[A-Za-z0-9_]+$")) {
						// force casting player
						Player target = Bukkit.getPlayer(args[1]);
						if (target == null) {
							sender.sendMessage(MagicSpells.textColor + "No matching player found");
							return true;
						}
						Spell spell = MagicSpells.getSpellByInGameName(args[2]);
						if (spell == null) {
							sender.sendMessage(MagicSpells.textColor + "No such spell");
							return true;
						}
						String[] spellArgs = null;
						if (args.length > 3) {
							spellArgs = Arrays.copyOfRange(args, 3, args.length);
						}
						spell.cast(target, spellArgs);
						sender.sendMessage(MagicSpells.textColor + "Player " + target.getName() + " forced to cast " + spell.getName());
					} else if (args[1].matches("^[^,]+,-?[0-9]+,-?[0-9]+,-?[0-9]+$")) {
						// force casting location
						String[] locData = args[1].split(",");
						World world = Bukkit.getWorld(locData[0]);
						if (world == null) {
							sender.sendMessage(MagicSpells.textColor + "No such world");
							return true;
						}
						Location loc = new Location(world, Integer.parseInt(locData[1]), Integer.parseInt(locData[2]), Integer.parseInt(locData[3]));
						Spell spell = MagicSpells.getSpellByInGameName(args[2]);
						if (spell == null) {
							sender.sendMessage(MagicSpells.textColor + "No such spell");
							return true;
						} else if (!(spell instanceof TargetedLocationSpell)) {
							sender.sendMessage(MagicSpells.textColor + "That spell cannot be cast at a location");
							return true;
						}
						boolean success = ((TargetedLocationSpell)spell).castAtLocation(loc, 1.0F);
						if (success) {
							sender.sendMessage(MagicSpells.textColor + "Spell " + spell.getName() + " casted at location " + args[1]);
						} else {
							sender.sendMessage(MagicSpells.textColor + "Spell " + spell.getName() + " failed to cast, may not be able to be cast at location");
						}
					} else {
						sender.sendMessage(MagicSpells.textColor + "Invalid forcecast target, must be playername or world,x,y,z");
					}
				} else if (sender.isOp() && args[0].equals("reload")) {
					if (args.length == 1) {
						plugin.unload();
						plugin.load();
						sender.sendMessage(MagicSpells.textColor + "MagicSpells config reloaded.");
					} else {
						List<Player> players = plugin.getServer().matchPlayer(args[1]);
						if (players.size() != 1) {
							sender.sendMessage(MagicSpells.textColor + "Player not found.");
						} else {
							Player player = players.get(0);
							MagicSpells.spellbooks.put(player.getName(), new Spellbook(player, plugin));
							sender.sendMessage(MagicSpells.textColor + player.getName() + "'s spellbook reloaded.");
						}
					}
				} else if (sender.isOp() && args[0].equals("resetcd")) {
					Player p = null;
					if (args.length > 1) {
						p = Bukkit.getPlayer(args[1]);
						if (p == null) {
							sender.sendMessage(MagicSpells.textColor + "No matching player found");
							return true;
						}
					}
					for (Spell spell : MagicSpells.spells.values()) {
						if (p != null) {
							spell.setCooldown(p, 0);
						} else {
							spell.getCooldowns().clear();
						}
					}
					sender.sendMessage(MagicSpells.textColor + "Cooldowns reset" + (p != null ? " for " + p.getName() : ""));
				} else if (sender.isOp() && args[0].equals("resetmana") && args.length > 1 && MagicSpells.mana != null) {
					Player p = Bukkit.getPlayer(args[1]);
					if (p != null) {
						MagicSpells.mana.createManaBar(p);
						MagicSpells.mana.addMana(p, MagicSpells.mana.getMaxMana(p), ManaChangeReason.OTHER);
						sender.sendMessage(MagicSpells.textColor + p.getName() + "'s mana reset.");
					}
				} else if (sender.isOp() && args[0].equals("magicitem") && args.length > 1 && sender instanceof Player) {
					ItemStack item = Util.getItemStackFromString(args[1]);
					if (item != null) {
						if (args.length > 2 && args[2].matches("^[0-9]+$")) {
							item.setAmount(Integer.parseInt(args[2]));
						}
						((Player)sender).getInventory().addItem(item);
					}
				} else if (sender.isOp() && args[0].equals("download") && args.length == 3) {
					File file = new File(plugin.getDataFolder(), "spells-" + args[1] + ".yml");
					if (file.exists()) {
						sender.sendMessage(MagicSpells.textColor + "ERROR: The file spells-" + args[1] + ".yml already exists!");
					} else {
						boolean downloaded = Util.downloadFile(args[2], file);
						if (downloaded) {
							sender.sendMessage(MagicSpells.textColor + "SUCCESS! You will need to do a /cast reload to load the new spells.");
						} else {
							sender.sendMessage(MagicSpells.textColor + "ERROR: The file could not be downloaded.");
						}
					}
				} else if (sender.isOp() && args[0].equals("profilereport")) {
					sender.sendMessage(MagicSpells.textColor + "Creating profiling report");
					MagicSpells.profilingReport();
				} else if (sender.isOp() && args[0].equals("debug")) {
					MagicSpells.debug = !MagicSpells.debug;
					sender.sendMessage("MagicSpells: debug mode " + (MagicSpells.debug?"enabled":"disabled"));
				} else if (sender instanceof Player) {
					Player player = (Player)sender;
					Spellbook spellbook = MagicSpells.getSpellbook(player);
					Spell spell = MagicSpells.getSpellByInGameName(args[0]);
					if (spell != null && spell.canCastByCommand() && spellbook.hasSpell(spell)) {
						if (spell.isValidItemForCastCommand(player.getItemInHand())) {
							String[] spellArgs = null;
							if (args.length > 1) {
								spellArgs = new String[args.length-1];
								for (int i = 1; i < args.length; i++) {
									spellArgs[i-1] = args[i];
								}
							}
							spell.cast(player, spellArgs);
						} else {
							MagicSpells.sendMessage(player, spell.getStrWrongCastItem());
						}
					} else {
						MagicSpells.sendMessage(player, MagicSpells.strUnknownSpell);
					}
				} else { // not a player
					Spell spell = MagicSpells.spellNames.get(args[0].toLowerCase());
					if (spell == null) {
						sender.sendMessage("Unknown spell.");
					} else {
						String[] spellArgs = null;
						if (args.length > 1) {
							spellArgs = new String[args.length-1];
							for (int i = 1; i < args.length; i++) {
								spellArgs[i-1] = args[i];
							}
						}
						boolean casted = false;
						if (sender instanceof BlockCommandSender) {
							if (spell instanceof TargetedLocationSpell) {
								((TargetedLocationSpell)spell).castAtLocation(((BlockCommandSender)sender).getBlock().getLocation(), 1.0F);
								casted = true;
							}
						}
						if (!casted) {
							boolean ok = spell.castFromConsole(sender, spellArgs);
							if (!ok) {
								sender.sendMessage("Cannot cast that spell from console.");
							}
						}
					}
				}
				return true;
			} else if (command.getName().equalsIgnoreCase("magicspellmana")) {
				if (MagicSpells.enableManaBars && sender instanceof Player) {
					Player player = (Player)sender;
					MagicSpells.mana.showMana(player, true);
				}
				return true;
			}
			return false;
		} catch (Exception ex) {
			MagicSpells.handleException(ex);
			sender.sendMessage(ChatColor.RED + "An error has occured.");
			return true;
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (enableTabComplete && sender instanceof Player) {
			Spellbook spellbook = MagicSpells.getSpellbook((Player)sender);
			String partial = Util.arrayJoin(args, ' ');
			return spellbook.tabComplete(partial);
		}
		return null;
	}

}
