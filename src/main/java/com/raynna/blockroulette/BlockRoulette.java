package com.raynna.blockroulette;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter.render.screen.ConfigSelectScreen;
import com.raynna.blockroulette.config.RaynnaServerConfig;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(BlockRoulette.MOD_ID)
public class BlockRoulette
{
    public static final String MOD_ID = "blockroulette";
    public static final String MOD_NAME = "Raynna's Block Roulette";

    public static final Logger LOGGER = LogUtils.getLogger();

    public static IProxy PROXY;
    public static BlockRoulette INSTANCE;



    public BlockRoulette(IEventBus modEventBus, ModContainer modContainer)
    {
        INSTANCE = this;
        PROXY = FMLEnvironment.dist == Dist.CLIENT
                ? new SideProxy.Client(modEventBus, modContainer)
                : new SideProxy.Server(modEventBus, modContainer);

        ConfigManager.getInstance().registerConfigHandler(RaynnaServerConfig.INSTANCE);
        ServerConfigManager.registerServerConfig(RaynnaServerConfig.INSTANCE, ServerConfigManager.PermissionChecker.IS_OPERATOR);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.Server.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.Client.SPEC);
        NeoForge.EVENT_BUS.register(this);

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info(MOD_NAME + " Mod loaded on dedicated server]");
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info(MOD_NAME + " Mod loaded on client]");
            event.enqueueWork(() -> {
                ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () ->
                        (mc, parent) -> new ConfigSelectScreen<>(
                                Component.translatable("config.raynna.common.title"),
                                parent,
                                RaynnaServerConfig.INSTANCE,
                                null
                        )
                );
            });
        }
    }
}
