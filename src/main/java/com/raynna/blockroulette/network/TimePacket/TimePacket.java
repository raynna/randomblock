package com.raynna.blockroulette.network.TimePacket;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimePacket(long gameTime, long lastBlockSpawn, long lastItemSpawn) implements CustomPacketPayload {

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final Type<TimePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("raynnarpg", "time_packet"));

    public static final StreamCodec<FriendlyByteBuf, TimePacket> CODEC = new StreamCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buf, TimePacket packet) {
            buf.writeLong(packet.gameTime());
            buf.writeLong(packet.lastBlockSpawn());
            buf.writeLong(packet.lastItemSpawn());
        }

        @Override
        public TimePacket decode(FriendlyByteBuf buf) {
            long gameTime = buf.readLong();
            long lastBlockSpawn = buf.readLong();
            long lastItemSpawn = buf.readLong();
            return new TimePacket(gameTime, lastBlockSpawn, lastItemSpawn);
        }
    };
}