package com.juubes.roentgen.datamineplugin;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.juubes.roentgen.datamineplugin.listeners.BlockListener;
import com.juubes.roentgen.datamineplugin.listeners.MoveListener;
import com.juubes.roentgen.datamineplugin.listeners.UseItemListener;
import com.zaxxer.hikari.HikariDataSource;

public class DataMinePlugin extends JavaPlugin {

	private final HikariDataSource HDS;
	private ProtocolManager protocolManager;

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

		HDS.setJdbcUrl("jdbc:mysql://" + host + ":3306/" + schema);
		HDS.setUsername(user);
		HDS.setPassword(password);
		HDS.addDataSourceProperty("cachePrepStmts", "true");
		HDS.addDataSourceProperty("prepStmtCacheSize", "250");
		HDS.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		HDS.setMaximumPoolSize(5);
		HDS.setConnectionTimeout(5000);

		this.protocolManager = ProtocolLibrary.getProtocolManager();

		// Create database
		try (Statement stmt = getHDS().getConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY)) {
			stmt.execute(IOUtils.toString(getResource("create-tables.sql"), Charset.forName("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Catch every time player moves or changes look direction
		protocolManager.addPacketListener(new MoveListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.POSITION_LOOK));
		protocolManager.addPacketListener(new MoveListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.POSITION));
		protocolManager.addPacketListener(new UseItemListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.USE_ENTITY));
		protocolManager.addPacketListener(new BlockListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.BLOCK_PLACE));
		protocolManager.addPacketListener(new BlockListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.BLOCK_DIG));

		// Removes data older than 10 minutes
		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			try (Statement stmt = getReusableConnection().createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_READ_ONLY)) {
				stmt.execute(IOUtils.toString(getResource("delete-over-10-minutes.sql"), Charset.forName("UTF-8")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 10, 5 * 20);

	}

	public HikariDataSource getHDS() {
		return HDS;
	}

	private Connection[] reusableConnections = new Connection[3];
	private int lastUsedIndex = 2;

	/**
	 * @return a living connection from HikariCP
	 **/
	public Connection getReusableConnection() {
		lastUsedIndex = (lastUsedIndex + 1) % reusableConnections.length;
		try {
			if (reusableConnections[lastUsedIndex] == null || reusableConnections[lastUsedIndex].isClosed())
				reusableConnections[lastUsedIndex] = HDS.getConnection();
			return reusableConnections[lastUsedIndex];
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new RuntimeException();
	}
}
