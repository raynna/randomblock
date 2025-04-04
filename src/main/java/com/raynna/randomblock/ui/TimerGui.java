package com.raynna.randomblock.ui;

import com.raynna.randomblock.Config;
import com.raynna.randomblock.events.SpawnRandomBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TimerGui {

    public static long gameTime = 0L;  // Store the game time from the server
    public static long lastBlockSpawn = 0L;  // Store the game time from the server

    public static void setTime(long newGameTime) {
        gameTime = newGameTime;
    }

    public static void setLastBlockSpawn(long newLastBlockSpawn) {
        lastBlockSpawn = newLastBlockSpawn;
    }

    public static void show(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (Config.Client.SHOW_GUI.get() == Config.Client.GuiMode.HIDE) return;

        long currentTime = gameTime;
        long interval = Config.Server.SPAWN_BLOCK_TIMER.get() * 20L;
        long timeSinceLastBlock = lastBlockSpawn;

       long elapsed = currentTime - timeSinceLastBlock;
       long remainingTicks = Math.max(0, interval - elapsed);
       long remainingSeconds = (remainingTicks + 19) / 20;
        String blockText = "§6Next Block Spawn: §f" + remainingSeconds + "s";
        String itemText = "§aNext Item Spawn: §f" + remainingSeconds + "s";


        int blockTextWidth = mc.font.width(Component.literal(blockText));
        int itemTextWidth = mc.font.width(Component.literal(itemText));


        int overlayWidth = Math.max(blockTextWidth, itemTextWidth) + 20;
        int overlayHeight = 30 + 10;

        int[] pos = GuiUtils.getPosition(
                Config.Client.GUI_POSITION.get(),
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                overlayWidth,
                overlayHeight
        );

        int x = pos[0];
        int y = pos[1];

        if (Config.Server.SPAWN_BLOCK_MODE.get() != Config.Server.SpawnBlockMode.OFF) {
            graphics.drawString(mc.font, Component.literal(blockText), x, y, 0xFFFFFF);
        }

        if (Config.Server.SPAWN_ITEM_MODE.get() != Config.Server.SpawnItemMode.OFF) {
            graphics.drawString(mc.font, Component.literal(itemText), x, y + 10, 0xFFFFFF);
        }
    }

    public static class GuiUtils {
        public static int[] getPosition(Config.Client.GuiPosition position, int screenWidth, int screenHeight, int overlayWidth, int overlayHeight) {
            int x = 0, y = 0;

            int xPadding = Config.Client.X_PADDING.get();
            int yPadding = Config.Client.Y_PADDING.get();

            switch (position) {
                case TOP_LEFT -> {
                    x = xPadding;
                    y = yPadding;
                }
                case TOP_CENTER -> {
                    x = (screenWidth - overlayWidth) / 2 + xPadding;
                    y = yPadding;
                }
                case TOP_RIGHT -> {
                    x = screenWidth - overlayWidth - xPadding;
                    y = yPadding;
                }
                case CENTER_LEFT -> {
                    x = xPadding;
                    y = (screenHeight - overlayHeight) / 2 + yPadding;
                }
                case CENTER -> {
                    x = (screenWidth - overlayWidth) / 2 + xPadding;
                    y = (screenHeight - overlayHeight) / 2 + yPadding;
                }
                case CENTER_RIGHT -> {
                    x = screenWidth - overlayWidth - xPadding;
                    y = (screenHeight - overlayHeight) / 2 + yPadding;
                }
                case BOTTOM_LEFT -> {
                    x = xPadding;
                    y = screenHeight - overlayHeight - yPadding;
                }
                case BOTTOM_CENTER -> {
                    x = (screenWidth - overlayWidth) / 2 + xPadding;
                    y = screenHeight - overlayHeight - yPadding;
                }
                case BOTTOM_RIGHT -> {
                    x = screenWidth - overlayWidth - xPadding;
                    y = screenHeight - overlayHeight - yPadding;
                }
            }

            return new int[]{x, y};
        }
    }
}
