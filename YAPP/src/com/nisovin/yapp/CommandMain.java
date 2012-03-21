package com.nisovin.yapp;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.entity.Player;

import com.nisovin.yapp.menu.Menu;

public class CommandMain implements CommandExecutor {
	
	private Map<CommandSender, PermissionContainer> selectedObject = new HashMap<CommandSender, PermissionContainer>();
	private Map<CommandSender, String> selectedWorld = new HashMap<CommandSender, String>();
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 0 && sender instanceof Conversable) {
			Menu.openMenu((Conversable)sender);
		} else if (args.length == 1) {
			if (args[0].equals("@") || args[0].equalsIgnoreCase("reload")) {
				// reload data
				MainPlugin.yapp.reload();
				sender.sendMessage(MainPlugin.TEXT_COLOR + "YAPP data reloaded");
			} else if (args[0].equals("?")) {
				// show status
				PermissionContainer obj = selectedObject.get(sender);
				String world = selectedWorld.get(sender);
				if (obj == null && world == null) {
					sender.sendMessage(MainPlugin.TEXT_COLOR + "You have nothing selected");
				} else if (obj == null && world != null) {
					sender.sendMessage(MainPlugin.TEXT_COLOR + "You have selected the world " + MainPlugin.HIGHLIGHT_COLOR + world);
				} else {
					String type = getType(obj);
					sender.sendMessage(MainPlugin.TEXT_COLOR + "You have selected the " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + 
							(world != null ? MainPlugin.TEXT_COLOR + " on world " + MainPlugin.HIGHLIGHT_COLOR + world : ""));
				}
			} else if (args[0].equalsIgnoreCase("delete")) {
				// deleting current object (no confirmation yet)
				delete(sender, false, alias);
			} else {
				// selecting something
				select(sender, args[0]);
			}
		} else if (args.length > 1 && args[0].matches("^[puxogwPUXOGW]:.*")) {
			// selecting something with multiple words
			String search = "";
			for (String a : args) {
				if (!search.isEmpty()) search += " ";
				search += a;
			}
			select(sender, search);
		} else if (args.length >= 2) {
			// get full arg
			String arg = "";
			for (int i = 1; i < args.length; i++) {
				if (!arg.isEmpty()) arg += " ";
				arg += args[i];
			}
			
			// performing an action
			if (args[0].equals("+")) {
				if (arg.startsWith("p:") || arg.startsWith("P:") || arg.startsWith("n:") || arg.startsWith("N:")) {
					// adding a permission
					addPermission(sender, arg.substring(2));
				} else if (arg.startsWith("g:") || arg.startsWith("G:")) {
					// adding a permission
					addGroup(sender, arg.substring(2));
				} else if (arg.contains(".")) {
					// adding a permission
					addPermission(sender, arg);
				} else {
					// adding a group
					addGroup(sender, arg);
				}
			} else if (args[0].equals("-")) {
				if (args[1].contains(".")) {
					// remove a permission
					removePermission(sender, arg);
				} else {
					// remove a group
					removeGroup(sender, arg);
				}
			} else if (args[0].equals("--")) {
				// negating a permission
				negatePermission(sender, arg);
			} else if (args[0].equalsIgnoreCase("+p") || args[0].equalsIgnoreCase("p+") || args[0].equalsIgnoreCase("+n") || args[0].equalsIgnoreCase("n+")) {
				// adding a permission
				addPermission(sender, arg);
			} else if (args[0].equalsIgnoreCase("+g") || args[0].equalsIgnoreCase("g+")) {
				// adding a group
				addGroup(sender, arg);
			} else if (args[0].equalsIgnoreCase("-p") || args[0].equalsIgnoreCase("p-") || args[0].equalsIgnoreCase("-n") || args[0].equalsIgnoreCase("n-")) {
				// removing a permission
				removePermission(sender, arg);
			} else if (args[0].equalsIgnoreCase("-g") || args[0].equalsIgnoreCase("g-")) {
				// removing a group
				removeGroup(sender, arg);
			} else if (args[0].equalsIgnoreCase("delete") && args[1].equals("CONFIRM")) {
				// deleting a group or player
				delete(sender, true, alias);
			} else if (args[0].equals("?")) {
				// checking for permission or group
				if (arg.startsWith("p:") || arg.startsWith("P:") || arg.startsWith("n:") || arg.startsWith("N:")) {
					checkPerm(sender, arg.substring(2));
				} else if (arg.startsWith("g:") || arg.startsWith("G:")) {
					checkGroup(sender, arg.substring(2));
				} else if (arg.contains(".")) {
					checkPerm(sender, arg);
				} else {
					checkGroup(sender, arg);
				}
			}
		}
		return true;
	}
	
	private void select(CommandSender sender, String search) {
		char mode = 'p';
		if (search.contains(":") && search.length() >= 2) {
			String[] s = search.split(":", 2);
			char c = s[0].toLowerCase().charAt(0);
			if (c == 'p' || c == 'u') {
				mode = 'p'; // normal player mode
			} else if (c == 'x' || c == 'o') {
				mode = 'x'; // exact player mode (offline)
			} else if (c == 'g') {
				mode = 'g'; // group mode
			} else if (c == 'w') {
				mode = 'w'; // world mode
			} else {
				sender.sendMessage("Invalid modifier '" + s[0] + "'");
				return;
			}
			search = s[1];
		}
		if (mode == 'p') {
			// select the player
			Player player = Bukkit.getServer().getPlayer(search);
			if (player != null) {
				User user = MainPlugin.getPlayerUser(player.getName());
				selectedObject.put(sender, user);
				sender.sendMessage(MainPlugin.TEXT_COLOR + "Selected player " + MainPlugin.HIGHLIGHT_COLOR + player.getName());
			} else {
				sender.sendMessage("No player found.");
			}
		} else if (mode == 'x') {
			// select the exact player or offline player
			User user = MainPlugin.getPlayerUser(search);
			selectedObject.put(sender, user);
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Selected player " + MainPlugin.HIGHLIGHT_COLOR + search);
		} else if (mode == 'g') {
			Group group = MainPlugin.getGroup(search);
			if (group == null) {
				group = MainPlugin.newGroup(search);
				sender.sendMessage(MainPlugin.TEXT_COLOR + "New group " + MainPlugin.HIGHLIGHT_COLOR + search + MainPlugin.TEXT_COLOR + " created");
			}
			selectedObject.put(sender, group);
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Selected group " + MainPlugin.HIGHLIGHT_COLOR + group.getName());
		} else if (mode == 'w') {
			if (search.isEmpty() || search.equals("-")) {
				selectedWorld.remove(sender);
				sender.sendMessage(MainPlugin.TEXT_COLOR + "Cleared world selection");
			} else {
				selectedWorld.put(sender, search);
				sender.sendMessage(MainPlugin.TEXT_COLOR + "Selected world " + MainPlugin.HIGHLIGHT_COLOR + search);
				if (Bukkit.getWorld(search) == null) {
					sender.sendMessage(MainPlugin.TEXT_COLOR + "   Warning: world " + MainPlugin.HIGHLIGHT_COLOR + search + MainPlugin.TEXT_COLOR + " is not loaded!");
				}
			}
		}
	}

	private void addPermission(CommandSender sender, String perm) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}		
		String world = selectedWorld.get(sender);		
		obj.addPermission(world, perm);

		String type = getType(obj);
		sender.sendMessage(MainPlugin.TEXT_COLOR + "Added permission " + MainPlugin.HIGHLIGHT_COLOR + perm + MainPlugin.TEXT_COLOR + " to " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName());
	}
	
	private void addGroup(CommandSender sender, String group) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}		
		String world = selectedWorld.get(sender);
		Group g = MainPlugin.getGroup(group);
		if (g == null) {
			g = MainPlugin.newGroup(group);
			sender.sendMessage(MainPlugin.TEXT_COLOR + "New group " + MainPlugin.HIGHLIGHT_COLOR + group + MainPlugin.TEXT_COLOR + " created");
		}
		obj.addGroup(world, g);

		String type = getType(obj);
		sender.sendMessage(MainPlugin.TEXT_COLOR + "Added group " + MainPlugin.HIGHLIGHT_COLOR + g.getName() + MainPlugin.TEXT_COLOR + " to " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName());
	}
	
	private void removePermission(CommandSender sender, String perm) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}
		String world = selectedWorld.get(sender);		
		boolean ok = obj.removePermission(world, perm);

		String type = getType(obj);
		if (ok) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Removed permission " + MainPlugin.HIGHLIGHT_COLOR + perm + MainPlugin.TEXT_COLOR + " from " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName());
		} else {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Unable to remove permission");
		}
	}
	
	private void removeGroup(CommandSender sender, String group) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}
		String world = selectedWorld.get(sender);

		Group g = MainPlugin.getGroup(group);
		if (g == null) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "The group " + MainPlugin.HIGHLIGHT_COLOR + group + MainPlugin.TEXT_COLOR + " does not exist");
			return;
		}
		boolean ok = obj.removeGroup(world, g);

		String type = getType(obj);
		if (ok) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Removed group " + MainPlugin.HIGHLIGHT_COLOR + g.getName() + MainPlugin.TEXT_COLOR + " from " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName());
		} else {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Unable to remove group");
		}
	}
	
	private void negatePermission(CommandSender sender, String perm) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}
		String world = selectedWorld.get(sender);
		obj.addPermission(world, "-" + perm);

		String type = getType(obj);		
		sender.sendMessage(MainPlugin.TEXT_COLOR + "Negated permission " + MainPlugin.HIGHLIGHT_COLOR + perm + MainPlugin.TEXT_COLOR + " for " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName());
	}
	
	private void checkPerm(CommandSender sender, String perm) {
		// get object
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}
		
		// get player if possible
		Player player = null;
		if (obj instanceof User) {
			player = ((User)obj).getPlayer();
		}

		// get world
		String world = selectedWorld.get(sender);

		// get type
		String type = getType(obj);
		
		boolean has = false;
		if (player != null) {
			has = player.hasPermission(perm);
		} else {
			has = obj.has(world, perm);
		}
		if (has) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " does have " + MainPlugin.TEXT_COLOR + "the permission " + MainPlugin.HIGHLIGHT_COLOR + perm);
		} else {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.RED + " does not have " + MainPlugin.TEXT_COLOR + "the permission " + MainPlugin.HIGHLIGHT_COLOR + perm);			
		}
	}
	
	private void checkGroup(CommandSender sender, String group) {
		// get object
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}

		// get world
		String world = selectedWorld.get(sender);
		
		// get group
		Group g = MainPlugin.getGroup(group);
		if (g == null) {
			sender.sendMessage(MainPlugin.ERROR_COLOR + "That group does not exist");
			return;
		}

		// get type
		String type = getType(obj);
		
		if (obj.inGroup(world, g, false)) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " directly inherits " + MainPlugin.TEXT_COLOR + "the group " + MainPlugin.HIGHLIGHT_COLOR + g.getName());
		} else if (obj.inGroup(world, g, true)) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " indirectly inherits " + MainPlugin.TEXT_COLOR + "the group " + MainPlugin.HIGHLIGHT_COLOR + g.getName());
		} else {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "The " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + ChatColor.RED + " does not inherit " + MainPlugin.TEXT_COLOR + "the group " + MainPlugin.HIGHLIGHT_COLOR + g.getName());
		}
	}
	
	private void delete(CommandSender sender, boolean confirmed, String alias) {
		// get working object
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}

		// get type
		String type = getType(obj);
		
		// send confirmation message
		if (!confirmed) {
			sender.sendMessage(MainPlugin.TEXT_COLOR + "Are you sure you want to delete the " + type + " " + MainPlugin.HIGHLIGHT_COLOR + obj.getName() + MainPlugin.TEXT_COLOR + "?");
			sender.sendMessage(MainPlugin.TEXT_COLOR + "To confirm type: " + MainPlugin.HIGHLIGHT_COLOR + "/" + alias + " delete CONFIRM");
			return;
		}
		
		if (obj instanceof User) {
			
		} else if (obj instanceof Group) {
			
		}
	}
	
	private void noObj(CommandSender sender) {
		sender.sendMessage(MainPlugin.TEXT_COLOR + "You have nothing selected!");
	}
	
	private String getType(PermissionContainer obj) {
		if (obj instanceof User) {
			return "player";
		} else if (obj instanceof Group) {
			return "group";
		} else {
			return "";
		}
	}
	
}
