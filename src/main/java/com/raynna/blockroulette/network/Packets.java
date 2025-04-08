package com.raynna.blockroulette.network;

import com.raynna.blockroulette.network.TimePacket.TimePacket;
import com.raynna.blockroulette.network.TimePacket.TimePacketHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class Packets {

    @SubscribeEvent
    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        String version = "1.0";
        PayloadRegistrar registrar = event.registrar(version);
        registerServerPackets(registrar);
        registerClientPackets(registrar);
    }

    public static void registerClientPackets(PayloadRegistrar registrar) {
        registrar.playToClient(TimePacket.TYPE, TimePacket.CODEC, new TimePacketHandler());
    }


    private static void registerServerPackets(PayloadRegistrar registrar) {
        //registrar.playToServer(ShiftClickPacket.TYPE, ShiftClickPacket.CODEC, new ShiftClickPacketHandler());
    }

}