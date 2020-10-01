package com.juubes.roentgen.datamineplugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BlockListener extends PacketAdapter {
	public static final int DATA_FORMAT_VERSION = 1;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public BlockListener(DataMinePlugin pl, ListenerPriority priority, PacketType packetType) {
		super(pl, priority, packetType);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		System.out.println(gson.toJson(event.getPacket().getHandle()));
	}

	@Override
	public DataMinePlugin getPlugin() {
		return (DataMinePlugin) this.plugin;
	}

}
