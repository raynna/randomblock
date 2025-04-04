package com.raynna.randomblock.network.TimePacket;

import net.minecraft.server.level.ServerPlayer;


public class TimePacketSender {

    public static void send(ServerPlayer player, long gameTime, long lastBlockSpawn) {
        TimePacket packet = new TimePacket(gameTime, lastBlockSpawn);
        player.connection.send(packet);
    }
}