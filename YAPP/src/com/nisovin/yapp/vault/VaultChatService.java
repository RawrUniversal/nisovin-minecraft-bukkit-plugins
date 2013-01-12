package com.nisovin.yapp.vault;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.MainPlugin;
import com.nisovin.yapp.User;

import net.milkbowl.vault.permission.Permission;

public class VaultChatService extends net.milkbowl.vault.chat.Chat {

	public VaultChatService(Permission perms) {
		super(perms);
	}

	@Override
	public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			String data = g.getInfo(world, node);
			if (data != null) {
				if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("yes")) {
					return true;
				} else if (data.equalsIgnoreCase("false") || data.equalsIgnoreCase("no")) {
					return false;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public double getGroupInfoDouble(String world, String group, String node, double defaultValue) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			String data = g.getInfo(world, node);
			if (data != null) {
				try {
					return Double.parseDouble(data);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public int getGroupInfoInteger(String world, String group, String node, int defaultValue) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			String data = g.getInfo(world, node);
			if (data != null) {
				try {
					return Integer.parseInt(data);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public String getGroupInfoString(String world, String group, String node, String defaultValue) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			String data = g.getInfo(world, node);
			if (data != null) {
				return data;
			}
		}
		return defaultValue;
	}

	@Override
	public String getGroupPrefix(String world, String group) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			return g.getPrefix(world);
		}
		return null;
	}

	@Override
	public String getGroupSuffix(String world, String group) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			return g.getInfo(world, "suffix");
		}
		return null;
	}

	@Override
	public String getName() {
		return "YAPP";
	}

	@Override
	public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			String data = u.getInfo(world, node);
			if (data != null) {
				if (data.equalsIgnoreCase("true") || data.equalsIgnoreCase("yes")) {
					return true;
				} else if (data.equalsIgnoreCase("false") || data.equalsIgnoreCase("no")) {
					return false;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public double getPlayerInfoDouble(String world, String player, String node, double defaultValue) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			String data = u.getInfo(world, node);
			if (data != null) {
				try {
					Double.parseDouble(data);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public int getPlayerInfoInteger(String world, String player, String node, int defaultValue) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			String data = u.getInfo(world, node);
			if (data != null) {
				try {
					Integer.parseInt(data);
				} catch (NumberFormatException e) {
					return defaultValue;
				}
			}
		}
		return defaultValue;
	}

	@Override
	public String getPlayerInfoString(String world, String player, String node, String defaultValue) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			return u.getInfo(world, node);
		}
		return defaultValue;
	}

	@Override
	public String getPlayerPrefix(String world, String player) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			return u.getPrefix(world);
		}
		return null;
	}

	@Override
	public String getPlayerSuffix(String world, String player) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			return u.getInfo(world, "suffix");
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return MainPlugin.yapp != null && MainPlugin.yapp.isEnabled();
	}

	@Override
	public void setGroupInfoBoolean(String world, String group, String node, boolean value) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			g.setInfo(world, node, value ? "true" : "false");
		}
	}

	@Override
	public void setGroupInfoDouble(String world, String group, String node, double value) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			g.setInfo(world, node, Double.toString(value));
		}
	}

	@Override
	public void setGroupInfoInteger(String world, String group, String node, int value) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			g.setInfo(world, node, Integer.toString(value));
		}
	}

	@Override
	public void setGroupInfoString(String world, String group, String node, String value) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			g.setInfo(world, node, value);
		}
	}

	@Override
	public void setGroupPrefix(String world, String group, String prefix) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			g.setPrefix(world, prefix);
		}
	}

	@Override
	public void setGroupSuffix(String world, String group, String suffix) {
		Group g = MainPlugin.getGroup(group);
		if (g != null) {
			g.setInfo(world, "suffix", suffix);
		}
	}

	@Override
	public void setPlayerInfoBoolean(String world, String player, String node, boolean value) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			u.setInfo(world, node, value ? "true" : "false");
		}
	}

	@Override
	public void setPlayerInfoDouble(String world, String player, String node, double value) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			u.setInfo(world, node, Double.toString(value));
		}
	}

	@Override
	public void setPlayerInfoInteger(String world, String player, String node, int value) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			u.setInfo(world, node, Integer.toString(value));
		}
	}

	@Override
	public void setPlayerInfoString(String world, String player, String node, String value) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			u.setInfo(world, node, value);
		}
	}

	@Override
	public void setPlayerPrefix(String world, String player, String prefix) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			u.setPrefix(world, prefix);
		}
	}

	@Override
	public void setPlayerSuffix(String world, String player, String suffix) {
		User u = MainPlugin.getPlayerUser(player);
		if (u != null) {
			u.setInfo(world, "suffix", suffix);
		}
	}

}
