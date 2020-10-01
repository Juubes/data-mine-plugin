package com.juubes.roentgen.datamineplugin.listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.juubes.roentgen.datamineplugin.DataMinePlugin;

public class MoveListener extends PacketAdapter {

	public static final int DATA_FORMAT_VERSION = 1;

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public MoveListener(DataMinePlugin pl, ListenerPriority priority, PacketType packetType) {
		super(pl, priority, packetType);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		// Convert to JSON and to an usable object
		PlayerMovePacket p = gson.fromJson(gson.toJson(event.getPacket().getHandle()), PlayerMovePacket.class);

		// Write data to mysql
		DataMinePlugin pl = (DataMinePlugin) this.plugin;
		Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			Connection conn = pl.getReusableConnection();
			try (PreparedStatement prep = conn.prepareStatement(
					"INSERT INTO Move (UUID, Date, Version, X, Y, Z, Yaw, Pitch, HasPos, HasLook, Gamemode, Health, WalkSpeed, FlySpeed, Sneaking, Sprinting, Blocking, ItemInHand) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
				prep.setString(1, event.getPlayer().getUniqueId().toString());
				prep.setLong(2, System.currentTimeMillis());
				prep.setInt(3, DATA_FORMAT_VERSION);
				prep.setFloat(4, p.x);
				prep.setFloat(5, p.y);
				prep.setFloat(6, p.z);
				prep.setFloat(7, p.yaw);
				prep.setFloat(8, p.pitch);
				prep.setBoolean(9, p.hasPos);
				prep.setBoolean(10, p.hasLook);
				prep.setInt(11, player.getGameMode().getValue());
				prep.setFloat(12, (float) player.getHealth());
				prep.setFloat(13, player.getWalkSpeed());
				prep.setFloat(14, player.getFlySpeed());
				prep.setBoolean(15, player.isSneaking());
				prep.setBoolean(16, player.isSprinting());
				prep.setBoolean(17, player.isBlocking());
				prep.setInt(18, player.getItemInHand().getTypeId());
				prep.execute();
			} catch (SQLException e) {
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