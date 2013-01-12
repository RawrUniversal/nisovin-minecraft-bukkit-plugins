package com.nisovin.yapp.storage;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.MySQLConnection;
import com.nisovin.yapp.Group;
import com.nisovin.yapp.YAPP;
import com.nisovin.yapp.PermissionContainer;
import com.nisovin.yapp.PermissionNode;

public class MySQLStorage implements StorageMethod {

	String host;
	String user;
	String pass;
	String db;
	
	MySQLConnection conn;	
	
	public MySQLStorage(String host, String user, String pass, String db) throws IOException {
		this.host = host;
		this.user = user;
		this.pass = pass;
		this.db = db;
		
		MySQLConnection conn = connect();
		if (conn == null) {
			throw new IOException("Unable to connect to database");
		}

		try {
			YAPP.debug("Checking database schema");
			createOrUpdateSchema(conn);
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IOException("Failed to create or update database schema");
		}
	}
	
	@Override
	public void fillGroupMap(Map<String, Group> groups) {
		MySQLConnection conn = connect();
		if (conn == null) return;
		
		try {
			Statement stmt = conn.createStatement();
			boolean hasResults = stmt.execute("SELECT DISTINCT n.`name` FROM " +
					"(SELECT `name` FROM `yapp_info` WHERE `type` = 2 UNION " +
					"SELECT `name` FROM `yapp_permissions` WHERE `type` = 2 UNION " +
					"SELECT `name` FROM `yapp_inherited_groups` WHERE `type` = 2 UNION " +
					"SELECT `group_name` FROM `yapp_inherited_groups`) n"
					);
			if (!hasResults) return;
			ResultSet results = stmt.getResultSet();
			while (results.next()) {
				String name = results.getString("name");
				if (!groups.containsKey(name.toLowerCase())) {
					Group group = new Group(name);
					groups.put(name.toLowerCase(), group);
					YAPP.debug("  Found group: " + name);
				}
			}
			results.close();
		} catch (SQLException e) {
			YAPP.error("MYSQL: ERROR GETTING GROUP LIST");
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				YAPP.error("MYSQL: ERROR GETTING GROUP LIST");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void load(PermissionContainer container) {
		MySQLConnection conn = connect();
		if (conn == null) return;
		
		String name = container.getName();
		int type = getContainerTypeId(container);
		
		try {
			// get info
			YAPP.debug("  Loading info");
			PreparedStatement stmt = conn.prepareStatement("SELECT `key`, `value` FROM `yapp_info` WHERE `name` = ? AND `type` = ? AND `world` = '' ORDER BY `order");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			if (stmt.execute()) {
				Map<String, String> info = container.getActualInfoMap();
				ResultSet results = stmt.getResultSet();
				while (results.next()) {
					info.put(results.getString("key"), results.getString("value"));
					YAPP.debug("    Added info: " + results.getString("key") + " = " + results.getString("value"));
				}
				results.close();
			}
			stmt.close();
			
			// get permissions
			YAPP.debug("  Loading permissions");
			stmt = conn.prepareStatement("SELECT `node`, `value` FROM `yapp_permissions` WHERE `name` = ? AND `type` = ? AND `world` = '' ORDER BY `order");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			if (stmt.execute()) {
				List<PermissionNode> perms = container.getActualPermissionList();
				ResultSet results = stmt.getResultSet();
				while (results.next()) {
					PermissionNode node = new PermissionNode(results.getString("node"), results.getInt("value") == 1 ? true : false);
					perms.add(node);
					YAPP.debug("    Added permission: " + node);
				}
				results.close();
			}
			stmt.close();
			
			// get groups
			YAPP.debug("  Loading groups");
			stmt = conn.prepareStatement("SELECT `group_name` FROM `yapp_inherited_groups` WHERE `name` = ? AND `type` = ? AND `world` = '' ORDER BY `order`");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			if (stmt.execute()) {
				List<Group> groups = container.getActualGroupList();
				ResultSet results = stmt.getResultSet();
				while (results.next()) {
					String groupName = results.getString("group_name");
					Group group = YAPP.getGroup(groupName);
					if (group != null) {
						boolean ok = true;
						// check for infinite group recursion
						if (container instanceof Group) {
							if (group.inheritsGroup(null, (Group)container)) {
								ok = false;
								YAPP.error("CIRCULAR GROUP REFERENCE DETECTED: while adding " + group.getName() + " to " + name);
							}
						}
						if (ok) {
							groups.add(group);
							YAPP.debug("    Added inherited group: " + group.getName());
						}
					} else {
						YAPP.warning(container.getType() + " '" + name + "' has non-existant inherited group '" + groupName + "'");
					}
				}
				results.close();
			}
			stmt.close();
			
			// get world info
			stmt = conn.prepareStatement("SELECT `key`, `value` FROM `yapp_info` WHERE `name` = ? AND `type` = ? AND `world` != '' ORDER BY `world`, `order`");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			if (stmt.execute()) {
				YAPP.debug("  Loading world info");
				Map<String, Map<String, String>> worldInfo = container.getActualWorldInfoMap();
				ResultSet results = stmt.getResultSet();
				while (results.next()) {
					String world = results.getString("world");
					Map<String, String> info = worldInfo.get(world);
					if (info == null) {
						info = new LinkedHashMap<String, String>();
						worldInfo.put(world, info);
					}
					info.put(results.getString("key"), results.getString("value"));
					YAPP.debug("    Added info for world " + world + ": " + results.getString("key") + " = " + results.getString("value"));
				}
				results.close();
			}
			stmt.close();
			
			// get world permissions
			stmt = conn.prepareStatement("SELECT `node`, `value` FROM `yapp_permissions` WHERE `name` = ? AND `type` = ? AND `world` != '' ORDER BY `world`, `order`");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			if (stmt.execute()) {
				YAPP.debug("  Loading world permissions");
				Map<String, List<PermissionNode>> worldPerms = container.getActualWorldPermissionMap();
				ResultSet results = stmt.getResultSet();
				while (results.next()) {
					String world = results.getString("world");
					List<PermissionNode> perms = worldPerms.get(world);
					if (perms == null) {
						perms = new ArrayList<PermissionNode>();
						worldPerms.put(world, perms);
					}
					PermissionNode node = new PermissionNode(results.getString("node"), results.getInt("value") == 1 ? true : false);
					perms.add(node);
					YAPP.debug("    Added permission for world " + world + ": " + node);
				}
				results.close();
			}
			stmt.close();
			
			// get world groups
			stmt = conn.prepareStatement("SELECT `group_name` FROM `yapp_inherited_groups` WHERE `name` = ? AND `type` = ? AND `world` != '' ORDER BY `world`, `order`");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			if (stmt.execute()) {
				YAPP.debug("  Loading world groups");
				Map<String, List<Group>> worldGroups = container.getActualWorldGroupMap();
				ResultSet results = stmt.getResultSet();
				while (results.next()) {
					String world = results.getString("world");
					List<Group> groups = worldGroups.get(world);
					if (groups == null) {
						groups = new ArrayList<Group>();
						worldGroups.put(world, groups);
					}
					String groupName = results.getString("group_name");
					Group group = YAPP.getGroup(groupName);
					if (group != null) {
						boolean ok = true;
						// check for infinite group recursion
						if (container instanceof Group) {
							if (group.inheritsGroup(world, (Group)container)) {
								ok = false;
								YAPP.error("CIRCULAR GROUP REFERENCE DETECTED: while adding " + group.getName() + " to " + name + " for world " + world);
							}
						}
						if (ok) {
							groups.add(group);
							YAPP.debug("    Added inherited group for world " + world + ": " + group.getName());
						}
					} else {
						YAPP.warning(container.getType() + " '" + name + "' has non-existant inherited group '" + groupName + "'");
					}
				}
				results.close();
			}
			stmt.close();
			
			container.setNotDirty();
			
		} catch (SQLException e) {
			YAPP.error("MYSQL: ERROR LOADING OBJECT: " + container.getType() + " " + name);
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void save(PermissionContainer container) {
		MySQLConnection conn = connect();
		if (conn == null) return;
		
		String name = container.getName();
		int type = getContainerTypeId(container);
		
		try {
			conn.setAutoCommit(false);
			
			// first delete existing data
			PreparedStatement stmt = conn.prepareStatement("DELETE FROM `yapp_info` WHERE `name` = ? AND `type` = ?");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			stmt.execute();
			stmt.close();
			stmt = conn.prepareStatement("DELETE FROM `yapp_permissions` WHERE `name` = ? AND `type` = ?");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			stmt.execute();
			stmt.close();
			stmt = conn.prepareStatement("DELETE FROM `yapp_inherited_groups` WHERE `name` = ? AND `type` = ?");
			stmt.setString(1, name);
			stmt.setInt(2, type);
			stmt.execute();
			stmt.close();
			
			int order = 0;
			
			// save info
			Map<String, String> info = container.getActualInfoMap();
			order = 0;
			for (String key : info.keySet()) {
				saveInfo(conn, name, type, "", key, info.get(key), order);
				order++;
			}
			
			// save perms
			List<PermissionNode> perms = container.getActualPermissionList();
			order = 0;
			for (PermissionNode node : perms) {
				savePermission(conn, name, type, "", node.getNodeName(), node.getValue(), order);
				order++;
			}
			
			// save groups
			List<Group> groups = container.getActualGroupList();
			order = 0;
			for (Group group : groups) {
				saveGroup(conn, name, type, "", group.getName(), order);
				order++;
			}
			
			// save world info
			Map<String, Map<String, String>> worldInfo = container.getActualWorldInfoMap();
			for (String world : worldInfo.keySet()) {
				info = worldInfo.get(world);
				order = 0;
				for (String key : info.keySet()) {
					saveInfo(conn, name, type, world, key, info.get(key), order);
					order++;
				}
			}
			
			// save world perms
			Map<String, List<PermissionNode>> worldPerms = container.getActualWorldPermissionMap();
			for (String world : worldPerms.keySet()) {
				perms = worldPerms.get(world);
				order = 0;
				for (PermissionNode node : perms) {
					savePermission(conn, name, type, world, node.getNodeName(), node.getValue(), order);
					order++;
				}
			}
			
			// save world groups
			Map<String, List<Group>> worldGroups = container.getActualWorldGroupMap();
			for (String world : worldGroups.keySet()) {
				groups = worldGroups.get(world);
				order = 0;
				for (Group group : groups) {
					saveGroup(conn, name, type, world, group.getName(), order);
					order++;
				}
			}
			
			conn.commit();
		} catch (SQLException e) {
			YAPP.error("MYSQL: ERROR SAVING OBJECT: " + container.getType() + " " + name);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				YAPP.error("MYSQL: ERROR ON ROLLBACK AFTER FAILED SAVE");
				e1.printStackTrace();
			}
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int getContainerTypeId(PermissionContainer container) {
		if (container.getType().equalsIgnoreCase("player")) {
			return 1;
		} else if (container.getType().equalsIgnoreCase("group")) {
			return 2;
		} else {
			return 0;
		}
	}
	
	private void saveInfo(MySQLConnection conn, String name, int type, String world, String key, String value, int order) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO `yapp_info` (`name`, `type`, `world`, `key`, `value`, `order`) VALUES (?, ?, ?, ?, ?, ?)");
		stmt.setString(1, name);
		stmt.setInt(2, type);
		stmt.setString(3, world);
		stmt.setString(4, key);
		stmt.setString(5, value);
		stmt.setInt(6, order);
		stmt.execute();
		stmt.close();
	}
	
	private void savePermission(MySQLConnection conn, String name, int type, String world, String node, boolean value, int order) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO `yapp_permissions` (`name`, `type`, `world`, `node`, `value`, `order`) VALUES (?, ?, ?, ?, ?, ?)");
		stmt.setString(1, name);
		stmt.setInt(2, type);
		stmt.setString(3, world);
		stmt.setString(4, node);
		stmt.setInt(5, value ? 1 : 0);
		stmt.setInt(6, order);
		stmt.execute();
		stmt.close();
	}
	
	private void saveGroup(MySQLConnection conn, String name, int type, String world, String group, int order) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement("INSERT INTO `yapp_inherited_groups` (`name`, `type`, `world`, `group_name`, `order`) VALUES (?, ?, ?, ?, ?)");
		stmt.setString(1, name);
		stmt.setInt(2, type);
		stmt.setString(3, world);
		stmt.setString(4, group);
		stmt.setInt(5, order);
		stmt.execute();
		stmt.close();
	}
	
	private MySQLConnection connect() {
		try {
			return (MySQLConnection) DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, user, pass);
		} catch (SQLException e) {
			YAPP.error("MYSQL: ERROR CONNECTING TO DATABASE");
			e.printStackTrace();
			return null;
		}
	}
	
	private void createOrUpdateSchema(MySQLConnection conn) throws SQLException {
		int currentVersion = 1;
		conn.setAutoCommit(false);
		try {
			int version = getSchemaVersion(conn);
			YAPP.debug("  Schema version is " + version);
			boolean update = false;
			if (version < 1) {
				YAPP.debug("  Updating schema to version 1");
				schemaUpdate1(conn);
				update = true;
			}
			if (update) {
				conn.createStatement().execute("UPDATE `yapp_data` SET `schema_version` = " + currentVersion);
				conn.commit();
			}
		} catch (SQLException e) {
			conn.rollback();
			throw e;
		}
	}
	
	private int getSchemaVersion(MySQLConnection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		stmt.execute("SHOW TABLES LIKE 'yapp_data'");
		ResultSet results = stmt.getResultSet();
		if (!results.first()) {
			results.close();
			stmt.close();
			return 0;
		} else {
			results.close();
			stmt.close();
		}
		stmt = conn.createStatement();
		boolean hasResults = stmt.execute("SELECT `schema_version` FROM `yapp_data`");
		if (!hasResults) {
			stmt.close();
			return 0;
		}
		results = stmt.getResultSet();
		if (!results.first()) {
			results.close();
			stmt.close();
			return 0;
		} else {
			int version = results.getInt("schema_version");
			results.close();
			stmt.close();
			return version;
		}
	}
	
	private void schemaUpdate1(MySQLConnection conn) throws SQLException {
		Statement stmt = conn.createStatement();
		
		// create yapp_data table
		stmt.execute("CREATE TABLE `yapp_data` (`schema_version` int(11) NOT NULL)");
		stmt.execute("INSERT INTO `yapp_data` (`schema_version`) VALUES (0)");
		
		// create yapp_info table
		stmt.execute("CREATE TABLE `yapp_info` (" +
						"`name` VARCHAR(50) NOT NULL ," +
						"`type` TINYINT(1) NOT NULL ," +
						"`world` VARCHAR(50) NOT NULL ," +
						"`key` VARCHAR(100) NOT NULL ," +
						"`value` VARCHAR(250) NOT NULL ," +
						"`order` INT NOT NULL ," +
						"PRIMARY KEY (`name`, `type`, `world`, `key`) )");
		
		// create permissions table
		stmt.execute("CREATE TABLE `yapp_permissions` (" +
						"`name` VARCHAR(50) NOT NULL ," +
						"`type` TINYINT(1) NOT NULL ," +
						"`world` VARCHAR(50) NOT NULL ," +
						"`node` VARCHAR(200) NOT NULL ," +
						"`value` TINYINT(1) NOT NULL ," +
						"`order` INT NOT NULL ," +
						"PRIMARY KEY (`name`, `type`, `world`, `node`) )");
		
		// create inherited groups table
		stmt.execute("CREATE TABLE `yapp_inherited_groups` (" +
						"`name` VARCHAR(50) NOT NULL ," +
						"`type` TINYINT(1) NOT NULL ," +
						"`world` VARCHAR(50) NOT NULL ," +
						"`group_name` VARCHAR(50) NOT NULL ," +
						"`order` INT NOT NULL ," +
						"PRIMARY KEY (`name`, `type`, `world`, `group_name`) )");
	}

}
