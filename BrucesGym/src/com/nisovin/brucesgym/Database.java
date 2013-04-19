package com.nisovin.brucesgym;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import com.mysql.jdbc.MySQLConnection;

public class Database {

	private BrucesGym plugin;
	private MySQLConnection connection;
	
	private Map<String, Integer> statisticIds = new HashMap<String, Integer>();
	private Map<String, StatisticType> statisticTypes = new HashMap<String, StatisticType>();
	
	public Database(BrucesGym plugin) {
		this.plugin = plugin;
	}
	
	public boolean connect(String host, String user, String pass, String db) {
		connection = openConnection(host, user, pass, db);
		return connection != null;
	}
	
	public void registerStatistic(GymGameMode gameMode, String name, StatisticType type) {
		try {
			int id = getStatisticId(name);
			if (id > 0) {
				statisticIds.put(name, id);
				statisticTypes.put(name, type);
			} else {
				id = createStatistic(name, gameMode, type);
				if (id > 0) {
					statisticIds.put(name, id);
					statisticTypes.put(name, type);				
				} else {
					plugin.getLogger().severe("FAILED TO REGISTER STATISTIC: " + name);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void performUpdates(Queue<DatabaseUpdate> updates) {
		while (!updates.isEmpty()) {
			DatabaseUpdate update = updates.poll();
			
			if (update instanceof StatisticUpdate) {
				performStatisticUpdate((StatisticUpdate)update);
			} else if (update instanceof KillUpdate) {
				performKillUpdate((KillUpdate)update);
			}
		}
	}
	
	private void performStatisticUpdate(StatisticUpdate update) {
		// get id and type
		if (!statisticIds.containsKey(update.statistic)) {
			plugin.getLogger().warning("ATTEMPTED TO UPDATE UNREGISTERED STATISTIC: " + update.statistic);
			return;
		}
		int id = statisticIds.get(update.statistic);
		StatisticType type = statisticTypes.get(update.statistic);
		
		// do update
		try {
			if (type == null) {
				return;
			} else if (type == StatisticType.XP || type == StatisticType.TOTAL) {
				int val = getStatisticValue(update.playerName, id);
				setStatisticValue(update.playerName, id, val + update.amount);
			} else if (type == StatisticType.MAX) {
				int val = getStatisticValue(update.playerName, id);
				if (update.amount > val) {
					setStatisticValue(update.playerName, id, update.amount);
				}
			} else if (type == StatisticType.MIN) {
				int val = getStatisticValue(update.playerName, id);
				if (update.amount < val || val == 0) {
					setStatisticValue(update.playerName, id, update.amount);
				}
			} else {
				return;
			}
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL ERROR ON STAT UPDATE: player=" + update.playerName + " stat=" + update.statistic);
			e.printStackTrace();
		}
	}
	
	private void performKillUpdate(KillUpdate update) {
		try {
			insertKillRecord(update.killerName, update.killedName, update.gameMode, update.weapon);
		} catch (SQLException e) {
			plugin.getLogger().severe("SQL ERROR ON KILL: killer=" + update.killerName + " killed=" + update.killedName);
			e.printStackTrace();
		}
	}
	
	private int getStatisticId(String name) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT `stat_id` FROM `gym_stats` WHERE `code` = ?");
		stmt.setString(1, name);
		if (stmt.execute()) {
			ResultSet results = stmt.getResultSet();
			if (results != null && results.next()) {
				return results.getInt("stat_id");
			}			
		}		
		return 0;
	}
	
	private int createStatistic(String name, GymGameMode gameMode, StatisticType type) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO `gym_stats` (`game_id`, `stat_type`, `code`, `name`) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
		stmt.setInt(1, gameMode.getId());
		stmt.setString(2, type.getCode());
		stmt.setString(3, name);
		stmt.setString(4, name);
		int count = stmt.executeUpdate();
		if (count == 1) {
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}
	
	private int getStatisticValue(String playerName, int id) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("SELECT `value` FROM `gym_stat_values` WHERE `player_name` = ? AND `stat_id` = ?");
		stmt.setString(1, playerName);
		stmt.setInt(2, id);
		if (stmt.execute()) {
			ResultSet results = stmt.getResultSet();
			if (results != null && results.next()) {
				return results.getInt("value");
			}
		}
		return 0;
	}
	
	private void setStatisticValue(String playerName, int id, int value) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("UPDATE `gym_stat_values` SET `value` = ? WHERE `player_name` = ? AND `stat_id` = ?");
		stmt.setInt(1, value);
		stmt.setString(2, playerName);
		stmt.setInt(3, id);
		int count = stmt.executeUpdate();
		if (count == 0) {
			// insert instead
			stmt = connection.prepareStatement("INSERT INTO `gym_stat_values` (`player_name`, `stat_id`, `value`) VALUES (?, ?, ?)");
			stmt.setString(1, playerName);
			stmt.setInt(2, id);
			stmt.setInt(3, value);
			stmt.executeUpdate();
		}
	}
	
	private void insertKillRecord(String killer, String killed, GymGameMode gameMode, String weapon) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement("INSERT INTO `gym_kills` (`killer`, `killed`, `when`, `game_id`, `weapon`) VALUES (?, ?, NOW(), ?, ?)");
		stmt.setString(1, killer);
		stmt.setString(2, killed);
		stmt.setInt(3, gameMode.getId());
		stmt.setString(4, weapon);
		stmt.executeUpdate();
	}
	
	private MySQLConnection openConnection(String host, String user, String pass, String db) {
		try {
			return (MySQLConnection) DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, user, pass);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
