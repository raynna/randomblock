package com.raynna.blockroulette.config;

import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.config.entry.IntegerEntry;
import com.iafenvoy.jupiter.interfaces.IConfigEnumEntry;
import org.jetbrains.annotations.NotNull;

public class ServerConfig extends AutoInitConfigContainer.AutoInitConfigCategoryBase {
        public final IntegerEntry SPAWN_ITEM_TIMER = new IntegerEntry("spawn_item_timer", 600, 1, Integer.MAX_VALUE);
        public final IntegerEntry SPAWN_BLOCK_TIMER = new IntegerEntry("spawn_block_timer", 60, 1, Integer.MAX_VALUE);


        public ServerConfig() {
            super("server", "raynna.server.tab");
        }

        public enum SpawnItemMode implements IConfigEnumEntry {
                ALL_PLAYERS("All Players"),
                ONE_IN_WORLD("One In World"),
                OFF("Off");

                private final String displayName;

                SpawnItemMode(String displayName) {
                        this.displayName = displayName;
                }

                @Override
                public String getName() {
                        return displayName;
                }

                @Override
                public @NotNull IConfigEnumEntry getByName(String name) {
                        try {
                                return valueOf(name);
                        } catch (IllegalArgumentException e) {
                                for (SpawnItemMode mode : values()) {
                                        if (mode.displayName.equalsIgnoreCase(name)) {
                                                return mode;
                                        }
                                }
                                return OFF;
                        }
                }

                @Override
                public IConfigEnumEntry cycle(boolean clockWise) {
                        int length = values().length;
                        int newOrdinal = (this.ordinal() + (clockWise ? 1 : length - 1)) % length;
                        return values()[newOrdinal];
                }
        }
    }