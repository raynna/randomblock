package com.raynna.randomblock.network.TimePacket;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record TimePacket(long gameTime) implements CustomPacketPayload {

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final Type<TimePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("raynnarpg", "time_packet"));

    public static final StreamCodec<FriendlyByteBuf, TimePacket> CODEC = new StreamCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buf, TimePacket packet) {
            buf.writeLong(packet.gameTime());
        }

        @Override
        public TimePacket decode(FriendlyByteBuf buf) {
            long gameTime = buf.readLong();
            return new TimePacket(gameTime);
        }
    };
}