package com.juubes.roentgen.datamineplugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataMinePlugin extends JavaPlugin {

	private ProtocolManager protocolManager;
	private final HikariDataSource HDS;

	/**
	 * Initializes HikariCP
	 */
	public DataMinePlugin() {
		this.HDS = new HikariDataSource();
	}

	@Override
	public void onEnable() {
		super.onEnable();

		saveDefaultConfig();

		Configuration conf = getConfig();
		String password = conf.getString("password");
		String user = conf.getString("user");
		String schema = conf.getString("schema");
		String host = conf.getString("host");

		getHDS().setJdbcUrl("jdbc:mysql://" + host + ":3306/" + schema);
		getHDS().setUsername(user);
		getHDS().setPassword(password);
		getHDS().addDataSourceProperty("cachePrepStmts", "true");
		getHDS().addDataSourceProperty("prepStmtCacheSize", "250");
		getHDS().addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		getHDS().setMaximumPoolSize(200);
		try {
			getHDS().getParentLogger().setLevel(Level.ALL);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		this.protocolManager = ProtocolLibrary.getProtocolManager();

		// Create database
		try (Statement stmt = getHDS().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			stmt.execute("CREATE TABLE IF NOT EXISTS Move (UUID VARCHAR(36), X FLOAT, Y FLOAT, Z FLOAT, "
					+ "Yaw FLOAT, Pitch FLOAT, HasPos BOOLEAN, HasLook BOOLEAN, Date BIGINT)");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// Catch every time player moves or changes look direction
		protocolManager.addPacketListener(new MoveListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.POSITION_LOOK));
		protocolManager.addPacketListener(new MoveListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.POSITION));

	}

	public HikariDataSource getHDS() {
		return HDS;
	}
}

class MoveListener extends PacketAdapter {
	public MoveListener(DataMinePlugin pl, ListenerPriority priority, PacketType packetType) {
		super(pl, priority, packetType);

	}

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void onPacketReceiving(PacketEvent event) {
		// Convert to JSON and to an usable object
		PlayerMovePacket p = gson.fromJson(gson.toJson(event.getPacket().getHandle()), PlayerMovePacket.class);

		// Write data to mysql
		DataMinePlugin pl = (DataMinePlugin) this.plugin;

		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			try (Connection conn = pl.getHDS().getConnection()) {
				try (PreparedStatement prep = conn.prepareStatement(
						"INSERT INTO Move (UUID, X, Y, Z, Yaw, Pitch, HasPos, HasLook, Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
					prep.setString(1, event.getPlayer().getUniqueId().toString());
					prep.setFloat(2, p.x);
					prep.setFloat(3, p.y);
					prep.setFloat(4, p.z);
					prep.setFloat(5, p.yaw);
					prep.setFloat(6, p.pitch);
					prep.setBoolean(7, p.hasPos);
					prep.setBoolean(8, p.hasLook);
					prep.setLong(9, System.currentTimeMillis());
					prep.execute();
					conn.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

	}

	private static class PlayerMovePacket {
		public float x;
		public float y;
		public float z;
		public float yaw;
		public float pitch;
		public boolean hasLook;
		public boolean hasPos;
	}
}
