package com.raynna.randomblock.network.TimePacket;

import net.minecraft.server.level.ServerPlayer;


public class TimePacketSender {

    public static void send(ServerPlayer player, long gameTime) {
        TimePacket packet = new TimePacket(gameTime);
        player.connection.send(packet);
    }
}