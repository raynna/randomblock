package com.raynna.blockroulette.network.TimePacket;

import net.minecraft.server.level.ServerPlayer;


public class TimePacketSender {

    public static void send(ServerPlayer player, long gameTime, long lastBlockSpawn, long lastItemSpawn) {
        TimePacket packet = new TimePacket(gameTime, lastBlockSpawn, lastItemSpawn);
        player.connection.send(packet);
    }
}