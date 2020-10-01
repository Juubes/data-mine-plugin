package com.juubes.roentgen.datamineplugin.listeners;

import java.lang.reflect.Field;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.juubes.roentgen.datamineplugin.DataMinePlugin;

public class UseItemListener extends PacketAdapter {
	public static final int DATA_FORMAT_VERSION = 1;
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public UseItemListener(DataMinePlugin pl, ListenerPriority priority, PacketType packetType) {
		super(pl, priority, packetType);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		for (Field f : event.getPacket().getHandle().getClass().getFields()) {
			System.out.println(gson.toJson(f.getName()));
		}
	}

	@Override
	public DataMinePlugin getPlugin() {
		return (DataMinePlugin) this.plugin;
	}

}
