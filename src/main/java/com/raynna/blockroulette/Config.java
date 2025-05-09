package com.raynna.blockroulette;

import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static com.raynna.blockroulette.BlockRoulette.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class Config {

    public static final int SERVER_VERSION = 1;
    public static final int CLIENT_VERSION = 1;

    public static final class Server {

        public static ModConfigSpec SPEC;
        public static final ModConfigSpec.IntValue SERVER_CONFIG_VERSION;

        public static final ModConfigSpec.IntValue SPAWN_ITEM_TIMER;
        public static final ModConfigSpec.IntValue SPAWN_BLOCK_TIMER;

        public static ModConfigSpec.EnumValue<SpawnItemMode> SPAWN_ITEM_MODE;
        public enum SpawnItemMode { ALL_PLAYERS, ONE_IN_WORLD, OFF }

        public static ModConfigSpec.EnumValue<SpawnBlockMode> SPAWN_BLOCK_MODE;
        public enum SpawnBlockMode { ALL_PLAYERS, ONE_IN_WORLD, OFF }

        public static ModConfigSpec.ConfigValue<List<? extends Integer>> SPAWN;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            SERVER_CONFIG_VERSION = builder.translation("Server Config Version: ").comment("DO NOT CHANGE. Used for tracking config updates.").defineInRange("config_version", SERVER_VERSION, 1, Integer.MAX_VALUE);
            SPAWN = builder.translation("Spawn Coordinates: ")
                    .comment("Enter your spawn coordinates for your server as a list: [x, y, z]")
                    .define("spawn", List.of(0, 68, 0), val -> val instanceof Integer);

            SPAWN_BLOCK_TIMER = builder.translation("Blocks/Second: ").comment("How frequent should new block spawn? In Seconds").comment("Default: 60").defineInRange("spawn_block_timer", 60, 1, Integer.MAX_VALUE);
            SPAWN_ITEM_TIMER = builder.translation("Items/Second: ").comment("How frequent should new item spawn? In Seconds").comment("Default: 600").defineInRange("spawn_item_timer", 600, 1, Integer.MAX_VALUE);

            SPAWN_BLOCK_MODE = builder.translation("Spawn Block Mode: ")
                    .comment("Determine if blocks should spawn for all players, or one per world, or shouldn't be spawned at all, Valid values are: ALL_PLAYERS, ONE_IN_WORLD, OFF")
                    .defineEnum("spawn_block_mode", SpawnBlockMode.ONE_IN_WORLD);

            SPAWN_ITEM_MODE = builder.translation("Spawn Item Mode: ")
                    .comment("Determine if items should spawn for all players, or one per world, or shouldn't be spawned at all, Valid values are: ALL_PLAYERS, ONE_IN_WORLD, OFF")
                    .defineEnum("spawn_item_mode", SpawnItemMode.ALL_PLAYERS);
            SPEC = builder.build();
        }

        public static BlockPos getSpawnPos() {
            List<? extends Integer> list = SPAWN.get();
            if (list.size() != 3) return new BlockPos(0, 68, 0); // fallback
            return new BlockPos(list.get(0), list.get(1), list.get(2));
        }

        public static void setSpawnPos(BlockPos pos) {
            SPAWN.set(List.of(pos.getX(), pos.getY(), pos.getZ()));
        }
    }


    public static final class Client {

        static ModConfigSpec SPEC;

        public static final ModConfigSpec.IntValue CLIENT_CONFIG_VERSION;

        public static ModConfigSpec.EnumValue<GuiMode> SHOW_GUI;
        public enum GuiMode { SHOW, HIDE }

        public enum GuiPosition {
            TOP_LEFT,
            TOP_CENTER,
            TOP_RIGHT,
            CENTER_LEFT,
            CENTER,
            CENTER_RIGHT,
            BOTTOM_LEFT,
            BOTTOM_CENTER,
            BOTTOM_RIGHT
        }

        public static ModConfigSpec.EnumValue<GuiPosition> GUI_POSITION;
        public static final ModConfigSpec.IntValue X_PADDING;
        public static final ModConfigSpec.IntValue Y_PADDING;

        static {
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            CLIENT_CONFIG_VERSION = builder.translation("Client Config Version: ").comment("DO NOT CHANGE. Used for tracking config updates.").defineInRange("config_version", CLIENT_VERSION, 1, Integer.MAX_VALUE);
            SHOW_GUI = builder.translation("Show Overlay: ")
                    .comment("Determine which timers should be shown or hidden")
                    .comment("Default: SHOW")
                    .defineEnum("show_gui", Client.GuiMode.SHOW);
            GUI_POSITION = builder.translation("Timer Overlay Position: ")
                    .comment("Location for timer overlay")
                    .defineEnum("gui_position", GuiPosition.CENTER_RIGHT);
            X_PADDING = builder.translation("Overlay X Adjustments: ").comment("Fine adjust the X position for the overlay").defineInRange("x_adjustment", 10, Integer.MIN_VALUE, Integer.MAX_VALUE);
            Y_PADDING = builder.translation("Overlay Y Adjustments: ").comment("Fine adjust the Y position for the overlay").defineInRange("y_adjustment", 10, Integer.MIN_VALUE, Integer.MAX_VALUE);

            SPEC = builder.build();
        }
    }

    @SubscribeEvent
    static void onReload(final ModConfigEvent.Loading event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == Server.SPEC) {
            int storedVersion = Server.SERVER_CONFIG_VERSION.get();
            if (storedVersion < SERVER_VERSION) {
                ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
                Path configFilePath = Paths.get("config", MOD_ID + "-common.toml");

                if (Files.exists(configFilePath)) {
                    try {
                        Files.delete(configFilePath);
                        System.out.println("Old config file deleted due to version change.");
                    } catch (IOException e) {
                        System.err.println("Failed to delete old config file: " + e.getMessage());
                    }
                }
                Server.SERVER_CONFIG_VERSION.set(SERVER_VERSION);
                Server.SPEC = builder.build();
                config.getSpec().validateSpec(config);
                assert config.getLoadedConfig() != null;
                config.getLoadedConfig().save();
                System.out.println("Config rebuilt due to version change.");
            }
        }
        if (config.getSpec() == Client.SPEC) {
            int storedVersion = Client.CLIENT_CONFIG_VERSION.get();
            if (storedVersion < CLIENT_VERSION) {
                ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
                Client.SPEC = builder.build();
            }
        }
    }
}