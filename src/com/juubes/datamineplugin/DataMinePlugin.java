package com.juubes.datamineplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class DataMinePlugin extends JavaPlugin {

	private ProtocolManager protocolManager;

	@Override
	public void onEnable() {
		super.onEnable();

		this.protocolManager = ProtocolLibrary.getProtocolManager();

		// Catch every time player moves or changes look direction
		protocolManager.addPacketListener(new MoveListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.POSITION_LOOK));
		protocolManager.addPacketListener(new MoveListener(this, ListenerPriority.NORMAL,
				PacketType.Play.Client.POSITION));

	}
}

class MoveListener extends PacketAdapter {
	private ObjectOutputStream outputWriter;
	private File outputFile;

	public MoveListener(DataMinePlugin pl, ListenerPriority priority, PacketType packetType) {
		super(pl, priority, packetType);
		try {
			File folder = new File(pl.getDataFolder(), "./packets/");
			folder.mkdirs();
			folder.createNewFile();
			this.outputFile = new File(folder, Instant.now().toString() + ".pstr");
			outputWriter = new ObjectOutputStream(new FileOutputStream(outputFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Override
	public void onPacketReceiving(PacketEvent event) {

		String json = gson.toJson(event.getPacket().getHandle());

		PlayerMovePacket p = gson.fromJson(json, PlayerMovePacket.class);

		// Write data to file without packet overhead

		try {
			if (outputFile.exists()) {
				outputFile.mkdirs();
				outputFile.createNewFile();
			}
			outputWriter.writeFloat(p.x);
			outputWriter.writeFloat(p.y);
			outputWriter.writeFloat(p.z);
			outputWriter.writeFloat(p.yaw);
			outputWriter.writeFloat(p.pitch);
			outputWriter.writeBoolean(p.hasPos);
			outputWriter.writeBoolean(p.hasLook);
			outputWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
