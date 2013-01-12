package com.nisovin.yapp.storage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.YAPP;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.PermissionNode;

public class FileStorage implements StorageMethod {


	@Override
	public void fillGroupMap(Map<String, Group> groups) {
		// get groups from group folder
		File groupsFolder = new File(YAPP.plugin.getDataFolder(), "groups");
		if (groupsFolder.exists() && groupsFolder.isDirectory()) {
			File[] groupFiles = groupsFolder.listFiles();
			for (File f : groupFiles) {
				if (f.getName().endsWith(".txt")) {
					String name = f.getName().replace(".txt", "");
					if (!groups.containsKey(name.toLowerCase())) {
						Group group = new Group(name);
						groups.put(name.toLowerCase(), group);
						YAPP.debug("  Found group: " + name);
					}
				}
			}
		}
		
		// get groups from world group folders
		File worldsFolder = new File(YAPP.plugin.getDataFolder(), "worlds");
		if (worldsFolder.exists() && worldsFolder.isDirectory()) {
			File[] worldFolders = worldsFolder.listFiles();
			for (File wf : worldFolders) {
				if (wf.isDirectory()) {
					File worldGroupsFolder = new File(wf, "groups");
					if (worldGroupsFolder.exists() && worldGroupsFolder.isDirectory()) {
						File[] groupFiles = worldGroupsFolder.listFiles();
						for (File f : groupFiles) {
							if (f.getName().endsWith(".txt")) {
								String name = f.getName().replace(".txt", "").toLowerCase();
								if (!groups.containsKey(name.toLowerCase())) {
									Group group = new Group(name);
									groups.put(name.toLowerCase(), group);
									YAPP.debug("  Found group: " + name);
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void load(PermissionContainer container) {
		String type = container.getType();
		String name = container.getName();
		File dataFolder = YAPP.plugin.getDataFolder();
		
		// get main file
		YAPP.debug("  Loading base data");
		File file = new File(dataFolder, type + "s" + File.separator + name + ".txt");
		if (file.exists()) {
			loadFromFile(container, file, container.getActualGroupList(), container.getActualPermissionList(), null);
		}
		
		// get world files
		file = new File(dataFolder, "worlds");
		if (file.exists()) {
			File[] files = file.listFiles();
			// find folders in plugin folder
			for (File f : files) {
				if (f.isDirectory()) {
					String worldName = f.getName();
					// get folder in world folder
					File groupsFolder = new File(f, type + "s");
					if (groupsFolder.exists() && groupsFolder.isDirectory()) {
						// get all files in folder
						File[] groupFiles = groupsFolder.listFiles();
						for (File groupFile : groupFiles) {
							if (groupFile.getName().equals(name + ".txt")) {
								// load file
								YAPP.debug("  Loading world data '" + worldName + "'");
								List<PermissionNode> perms = new ArrayList<PermissionNode>();
								container.getActualWorldPermissionMap().put(worldName, perms);
								List<Group> wgroups = new ArrayList<Group>();
								container.getActualWorldGroupMap().put(worldName, wgroups);
								loadFromFile(container, groupFile, wgroups, perms, worldName);
							}
						}
					}
				}
			}
		}
		
		container.setNotDirty();
	}
	
	private void loadFromFile(PermissionContainer container, File file, List<Group> groups, List<PermissionNode> perms, String worldName) {
		try {
			String name = container.getName();
			String type = container.getType();
			String mode = "";
			Scanner scanner = new Scanner(file);
			String line;
			while (scanner.hasNext()) {
				line = scanner.nextLine().trim();
				if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
					// ignore
				} else if (line.startsWith("=")) {
					// mode change
					line = line.replace("=", "").trim().toLowerCase();
					if (line.startsWith("data") || line.startsWith("info")) {
						mode = "info";
						YAPP.debug("    Reading info");
					} else if (line.startsWith("inherit") || line.startsWith("group")) {
						mode = "groups";
						YAPP.debug("    Reading groups");
					} else if (line.startsWith("perm")) {
						mode = "perms";
						YAPP.debug("    Reading perms");
					}
				} else if (mode.equals("info")) {
					String key = null, val = null;
					if (line.contains("=")) {
						String[] s = line.split("=", 2);
						key = s[0].trim();
						val = s[1].trim();
					} else if (line.contains(":")) {
						String[] s = line.split(":", 2);
						key = s[0].trim();
						val = s[1].trim();
					}
					if (key != null && val != null) {
						key = key.toLowerCase();
						if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
							val = val.substring(1, val.length() - 1);
						}
						container.setInfo(worldName, key, val);
						container.setNotDirty();
						YAPP.debug("      Added info: " + key + " = " + val);
					} else {
						YAPP.warning(type + " '" + name + "' has invalid info line: " + line);
					}
				} else if (mode.equals("groups")) {
					// inherited group
					Group group = YAPP.getGroup(line);
					if (group != null) {
						boolean ok = true;
						// check for infinite group recursion
						if (container instanceof Group) {
							if (group.inheritsGroup(worldName, (Group)container)) {
								ok = false;
								YAPP.error("CIRCULAR GROUP REFERENCE DETECTED: while adding " + group.getName() + " to " + name);
							}
						}
						if (ok) {
							groups.add(group);
							YAPP.debug("      Added inherited group: " + line);
						}
					} else {
						YAPP.warning(type + " '" + name + "' has non-existant inherited group '" + line + "'");
					}
				} else if (mode.equals("perms")) {
					// permission
					PermissionNode node = new PermissionNode(line);
					perms.add(node);
					YAPP.debug("      Added permission: " + node);
				} else {
					YAPP.warning(type + " '" + name + "' has orphan line: " + line);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void save(PermissionContainer container) {
		String name = container.getName();
		String type = container.getType();
		container.setNotDirty();
		
		BufferedWriter file = null;
		
		// save base data
		try {
			file = writer(container, null);
			// save info
			Map<String, String> info = container.getActualInfoMap();
			if (info.size() > 0) {
				writeInfo(file, info);
			}
			// save groups
			List<Group> groups = container.getActualGroupList();
			if (groups.size() > 0) {
				writeGroups(file, groups);
			}
			// save perms
			List<PermissionNode> permissions = container.getActualPermissionList();
			writePermissions(file, permissions);
			file.close();
		} catch (IOException e) {
			YAPP.error("Failed to write file for " + type + " '" + name + "'!");
		}
		
		// save world data
		Map<String, List<PermissionNode>> worldPermissions = container.getActualWorldPermissionMap();
		Map<String, List<Group>> worldGroups = container.getActualWorldGroupMap();
		Map<String, Map<String, String>> worldInfo = container.getActualWorldInfoMap();
		Set<String> worldNames = new HashSet<String>();
		worldNames.addAll(worldPermissions.keySet());
		worldNames.addAll(worldGroups.keySet());
		worldNames.addAll(worldInfo.keySet());
		Map<String, String> winfo = null;
		List<Group> wgroups = null;
		List<PermissionNode> wperms = null;
		for (String worldName : worldNames) {
			try {
				file = writer(container, worldName);
				winfo = worldInfo.get(worldName);
				wgroups = worldGroups.get(worldName);
				wperms = worldPermissions.get(worldName);
				// save info
				if (winfo != null && winfo.size() > 0) {
					writeInfo(file, winfo);
				}
				// save groups
				if (wgroups != null && wgroups.size() > 0) {
					writeGroups(file, wgroups);
				}
				// save perms
				if (wperms != null) {
					writePermissions(file, wperms);
				}
				file.close();
			} catch (IOException e) {
				YAPP.error("Failed to write file for " + type + " '" + name + "' for world '" + worldName + "'!");
			}
		}
	}
	
	private void writeInfo(BufferedWriter file, Map<String, String> info) throws IOException {
		file.write("== INFORMATION ==");
		file.newLine();
		file.newLine();
		for (String key : info.keySet()) {
			file.write(key + " : \"" + info.get(key) + "\"");
			file.newLine();
		}
		file.newLine();
	}
	
	private void writeGroups(BufferedWriter file, List<Group> groups) throws IOException {
		file.write("== GROUPS ==");
		file.newLine();
		file.newLine();
		for (Group g : groups) {
			file.write(g.getName());
			file.newLine();
		}
		file.newLine();
	}
	
	private void writePermissions(BufferedWriter file, List<PermissionNode> perms) throws IOException {
		file.write("== PERMISSIONS ==");
		file.newLine();
		file.newLine();
		for (PermissionNode n : perms) {
			file.write((n.getValue() == true ? " + " : " - ") + n.getNodeName());
			file.newLine();
		}
		file.newLine();
	}
	
	private BufferedWriter writer(PermissionContainer container, String worldName) throws IOException {
		File file;
		if (worldName == null) {
			file = new File(YAPP.plugin.getDataFolder(), container.getType() + "s" + File.separator + container.getName() + ".txt");
		} else {
			file = new File(YAPP.plugin.getDataFolder(), "worlds" + File.separator + worldName + File.separator + container.getType() + "s" + File.separator + container.getName() + ".txt");
		}
		file.mkdirs();
		if (file.exists()) file.delete();
		
		return new BufferedWriter(new FileWriter(file));
	}

}
