package com.raynna.blockroulette.events;

import com.raynna.blockroulette.BlockRoulette;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.util.Map;

@EventBusSubscriber(modid = BlockRoulette.MOD_ID, value = Dist.CLIENT)
public class LoginEvents {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        BlockRoulette.LOGGER.info("Syncing Configs on client join.");
    }

    @SubscribeEvent
    public static void onServerShutdown(ServerStoppedEvent event) {
        SpawnRandomBlock.PlacedBlock block = SpawnRandomBlock.getLastPlacedBlock();
        if (block != null) {
            event.getServer().overworld().removeBlock(block.pos(), false);
        }
        Map<ServerPlayer, SpawnRandomBlock.PlacedBlock> blocks = SpawnRandomBlock.getPlacedBlocks();
        if (blocks != null) {
            for (SpawnRandomBlock.PlacedBlock entry : blocks.values()) {
                if (entry == null) continue;
                event.getServer().overworld().removeBlock(entry.pos(), false);
            }
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(LoginEvents.class);
    }
}
