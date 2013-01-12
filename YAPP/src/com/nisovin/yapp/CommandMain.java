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
		} else {
			// gather args
			StringBuilder sb = new StringBuilder();
			sb.append(args[0]);
			for (int i = 1; i < args.length; i++) {
				sb.append(' ');
				sb.append(args[i]);
			}
			String[] commands = sb.toString().split("\\|");
			for (String c : commands) {
				c = c.trim();
				if (c.isEmpty()) continue;
				args = c.split(" ");
				if (args.length == 1) {
					if (args[0].equals("@") || args[0].equalsIgnoreCase("reload")) {
						// reload data
						YAPP.plugin.reload();
						sender.sendMessage(YAPP.TEXT_COLOR + "YAPP data reloaded");
					} else if (args[0].equals("?")) {
						// show status
						PermissionContainer obj = selectedObject.get(sender);
						String world = selectedWorld.get(sender);
						if (obj == null && world == null) {
							sender.sendMessage(YAPP.TEXT_COLOR + "You have nothing selected");
						} else if (obj == null && world != null) {
							sender.sendMessage(YAPP.TEXT_COLOR + "You have selected the world " + YAPP.HIGHLIGHT_COLOR + world);
						} else {
							String type = getType(obj);
							sender.sendMessage(YAPP.TEXT_COLOR + "You have selected the " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + 
									(world != null ? YAPP.TEXT_COLOR + " on world " + YAPP.HIGHLIGHT_COLOR + world : ""));
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
					String search = arrayJoin(args, 0);
					select(sender, search);
				} else if (args.length >= 2) {
					// get full arg
					String arg = arrayJoin(args, 1);
					
					// performing an action
					if (args[0].equals("@")) {
						// refreshing player permissions
						refresh(sender, args[1]);
					} else if (args[0].equals("+")) {
						if (arg.startsWith("n:") || arg.startsWith("N:")) {
							// adding a permission
							addPermission(sender, arg.substring(2));
						} else if (arg.startsWith("g:") || arg.startsWith("G:")) {
							// adding a permission
							addGroup(sender, arg.substring(2));
						} else if (arg.startsWith("p:") || arg.startsWith("P:")) {
							// adding a player to a group
							// TODO
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
					} else if (args[0].equalsIgnoreCase("+n") || args[0].equalsIgnoreCase("n+")) {
						// adding a permission
						addPermission(sender, arg);
					} else if (args[0].equalsIgnoreCase("+g") || args[0].equalsIgnoreCase("g+")) {
						// adding a group
						addGroup(sender, arg);
					} else if (args[0].equalsIgnoreCase("=g") || args[0].equalsIgnoreCase("g=") || args[0].equalsIgnoreCase("g")) {
						// setting a group
						setGroup(sender, arg);
					} else if (args[0].equalsIgnoreCase("-n") || args[0].equalsIgnoreCase("n-")) {
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
					} else if (args[0].equals("=")) {
						set(sender, args[1], arrayJoin(args, 2));
					} else if (args[1].equals("=")) {
						set(sender, args[0], arrayJoin(args, 2));
					}
				}
			}
		}
		return true;
	}
	
	private String arrayJoin(String[] array, int start) {
		String s = "";
		for (int i = start; i < array.length; i++) {
			if (!s.isEmpty()) s += " ";
			s += array[i];
		}
		return s;
	}
	
	private void refresh(CommandSender sender, String name) {
		Player player = Bukkit.getPlayer(name);
		if (player != null) {
			YAPP.plugin.loadPlayerPermissions(player);
			sender.sendMessage(YAPP.TEXT_COLOR + "Refreshed permissions for player " + YAPP.HIGHLIGHT_COLOR + player.getName());
		} else {
			sender.sendMessage("No player found.");
		}
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
				User user = YAPP.getPlayerUser(player.getName());
				selectedObject.put(sender, user);
				sender.sendMessage(YAPP.TEXT_COLOR + "Selected player " + YAPP.HIGHLIGHT_COLOR + player.getName());
			} else {
				sender.sendMessage("No player found.");
			}
		} else if (mode == 'x') {
			// select the exact player or offline player
			User user = YAPP.getPlayerUser(search);
			selectedObject.put(sender, user);
			sender.sendMessage(YAPP.TEXT_COLOR + "Selected player " + YAPP.HIGHLIGHT_COLOR + search);
		} else if (mode == 'g') {
			Group group = YAPP.getGroup(search);
			if (group == null) {
				group = YAPP.newGroup(search);
				sender.sendMessage(YAPP.TEXT_COLOR + "New group " + YAPP.HIGHLIGHT_COLOR + search + YAPP.TEXT_COLOR + " created");
			}
			selectedObject.put(sender, group);
			sender.sendMessage(YAPP.TEXT_COLOR + "Selected group " + YAPP.HIGHLIGHT_COLOR + group.getName());
		} else if (mode == 'w') {
			if (search.isEmpty() || search.equals("-")) {
				selectedWorld.remove(sender);
				sender.sendMessage(YAPP.TEXT_COLOR + "Cleared world selection");
			} else {
				selectedWorld.put(sender, search);
				sender.sendMessage(YAPP.TEXT_COLOR + "Selected world " + YAPP.HIGHLIGHT_COLOR + search);
				if (Bukkit.getWorld(search) == null) {
					sender.sendMessage(YAPP.TEXT_COLOR + "   Warning: world " + YAPP.HIGHLIGHT_COLOR + search + YAPP.TEXT_COLOR + " is not loaded!");
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
		sender.sendMessage(YAPP.TEXT_COLOR + "Added permission " + YAPP.HIGHLIGHT_COLOR + perm + YAPP.TEXT_COLOR + " to " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName());
	}
	
	private void addGroup(CommandSender sender, String group) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}		
		String world = selectedWorld.get(sender);
		Group g = YAPP.getGroup(group);
		if (g == null) {
			g = YAPP.newGroup(group);
			sender.sendMessage(YAPP.TEXT_COLOR + "New group " + YAPP.HIGHLIGHT_COLOR + group + YAPP.TEXT_COLOR + " created");
		}
		boolean added = obj.addGroup(world, g);

		if (added) {
			String type = getType(obj);
			sender.sendMessage(YAPP.TEXT_COLOR + "Added group " + YAPP.HIGHLIGHT_COLOR + g.getName() + YAPP.TEXT_COLOR + " to " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName());
		} else {
			sender.sendMessage(YAPP.ERROR_COLOR + "Failed to add group!");
		}
	}
	
	private void setGroup(CommandSender sender, String group) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}		
		String world = selectedWorld.get(sender);
		Group g = YAPP.getGroup(group);
		if (g == null) {
			g = YAPP.newGroup(group);
			sender.sendMessage(YAPP.TEXT_COLOR + "New group " + YAPP.HIGHLIGHT_COLOR + group + YAPP.TEXT_COLOR + " created");
		}
		boolean set = obj.setGroup(world, g);
		
		if (set) {
			String type = getType(obj);
			sender.sendMessage(YAPP.TEXT_COLOR + "Set group for " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + YAPP.TEXT_COLOR + " to " + YAPP.HIGHLIGHT_COLOR + g.getName());
		} else {
			sender.sendMessage(YAPP.ERROR_COLOR + "Failed to set group!");
		}
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
			sender.sendMessage(YAPP.TEXT_COLOR + "Removed permission " + YAPP.HIGHLIGHT_COLOR + perm + YAPP.TEXT_COLOR + " from " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName());
		} else {
			sender.sendMessage(YAPP.TEXT_COLOR + "Unable to remove permission");
		}
	}
	
	private void removeGroup(CommandSender sender, String group) {
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}
		String world = selectedWorld.get(sender);

		Group g = YAPP.getGroup(group);
		if (g == null) {
			sender.sendMessage(YAPP.TEXT_COLOR + "The group " + YAPP.HIGHLIGHT_COLOR + group + YAPP.TEXT_COLOR + " does not exist");
			return;
		}
		boolean ok = obj.removeGroup(world, g);

		String type = getType(obj);
		if (ok) {
			sender.sendMessage(YAPP.TEXT_COLOR + "Removed group " + YAPP.HIGHLIGHT_COLOR + g.getName() + YAPP.TEXT_COLOR + " from " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName());
		} else {
			sender.sendMessage(YAPP.TEXT_COLOR + "Unable to remove group");
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
		sender.sendMessage(YAPP.TEXT_COLOR + "Negated permission " + YAPP.HIGHLIGHT_COLOR + perm + YAPP.TEXT_COLOR + " for " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName());
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
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " does have " + YAPP.TEXT_COLOR + "the permission " + YAPP.HIGHLIGHT_COLOR + perm);
		} else {
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + ChatColor.RED + " does not have " + YAPP.TEXT_COLOR + "the permission " + YAPP.HIGHLIGHT_COLOR + perm);			
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
		Group g = YAPP.getGroup(group);
		if (g == null) {
			sender.sendMessage(YAPP.ERROR_COLOR + "That group does not exist");
			return;
		}

		// get type
		String type = getType(obj);
		
		if (obj.inGroup(world, g, false)) {
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " directly inherits " + YAPP.TEXT_COLOR + "the group " + YAPP.HIGHLIGHT_COLOR + g.getName());
		} else if (obj.inGroup(world, g, true)) {
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + ChatColor.GREEN + " indirectly inherits " + YAPP.TEXT_COLOR + "the group " + YAPP.HIGHLIGHT_COLOR + g.getName());
		} else {
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + ChatColor.RED + " does not inherit " + YAPP.TEXT_COLOR + "the group " + YAPP.HIGHLIGHT_COLOR + g.getName());
		}
	}
	
	private void set(CommandSender sender, String key, String value) {
		// get object
		PermissionContainer obj = selectedObject.get(sender);
		if (obj == null) {
			noObj(sender);
			return;
		}
		
		// get type
		String type = getType(obj);

		// get world
		String world = selectedWorld.get(sender);
		
		// set the info
		obj.setInfo(world, key, value);
		// TODO: value = obj.getInfo(world, key);
		
		if (value != null) {
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + YAPP.TEXT_COLOR + "'s " + 
					YAPP.HIGHLIGHT_COLOR + key + YAPP.TEXT_COLOR + " has been set to: " + ChatColor.WHITE + value);
		} else {
			sender.sendMessage(YAPP.TEXT_COLOR + "The " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + YAPP.TEXT_COLOR + "'s " + 
				YAPP.HIGHLIGHT_COLOR + key + YAPP.TEXT_COLOR + " is empty");
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
			sender.sendMessage(YAPP.TEXT_COLOR + "Are you sure you want to delete the " + type + " " + YAPP.HIGHLIGHT_COLOR + obj.getName() + YAPP.TEXT_COLOR + "?");
			sender.sendMessage(YAPP.TEXT_COLOR + "To confirm type: " + YAPP.HIGHLIGHT_COLOR + "/" + alias + " delete CONFIRM");
			return;
		}
		
		if (obj instanceof User) {
			
		} else if (obj instanceof Group) {
			
		}
	}
	
	private void noObj(CommandSender sender) {
		sender.sendMessage(YAPP.TEXT_COLOR + "You have nothing selected!");
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
