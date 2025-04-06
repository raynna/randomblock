package com.raynna.randomblock.network.TimePacket;

import com.raynna.randomblock.ui.TimerGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public class TimePacketHandler implements IPayloadHandler<TimePacket> {

    @Override
    public void handle(TimePacket packet, IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
           handleClient(packet);//test
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static void handleClient(TimePacket packet) {
        Minecraft.getInstance().execute(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            TimerGui.setTime(packet.gameTime());
            TimerGui.setLastBlockSpawn(packet.lastBlockSpawn());
            TimerGui.setLastItemSpawn(packet.lastItemSpawn());
        });
    }
}