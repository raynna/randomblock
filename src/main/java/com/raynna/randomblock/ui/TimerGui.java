package com.raynna.randomblock.ui;

import com.raynna.randomblock.Config;
import com.raynna.randomblock.events.SpawnRandomBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TimerGui {

    public static void show(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        if (Config.Client.SHOW_GUI.get() == Config.Client.GuiMode.HIDE) return;

        long currentTime = mc.level.getGameTime();

        long blockInterval = Config.Server.SPAWN_BLOCK_TIMER.get() * 20L;
        long timeSinceLastBlock = currentTime - SpawnRandomBlock.getLastBlockSpawnTime();
        long blockRemaining = Mth.clamp(blockInterval - timeSinceLastBlock, 0, blockInterval) / 20;

        long itemInterval = Config.Server.SPAWN_ITEM_TIMER.get() * 20L;
        long timeSinceLastItem = currentTime - SpawnRandomBlock.getLastItemSpawnTime();
        long itemRemaining = Mth.clamp(itemInterval - timeSinceLastItem, 0, itemInterval) / 20;

        String blockText = "§6Next Block Spawn: §f" + blockRemaining + "s";
        String itemText = "§aNext Item Spawn: §f" + itemRemaining + "s";


        int blockTextWidth = mc.font.width(Component.literal(blockText));  // Get the width of the block spawn text
        int itemTextWidth = mc.font.width(Component.literal(itemText));    // Get the width of the item spawn text


        int overlayWidth = Math.max(blockTextWidth, itemTextWidth) + 20;  // Add some padding
        int overlayHeight = 30 + 10;  // Height for both lines of text

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

            switch (position) {
                case TOP_LEFT -> {
                    x = Config.Client.X_PADDING.get();
                    y = Config.Client.Y_PADDING.get();
                }
                case TOP_CENTER -> {
                    x = (screenWidth - overlayWidth) / 2;
                    y = Config.Client.Y_PADDING.get();
                }
                case TOP_RIGHT -> {
                    x = screenWidth - overlayWidth - Config.Client.X_PADDING.get();
                    y = Config.Client.Y_PADDING.get();
                }
                case CENTER_LEFT -> {
                    x = Config.Client.X_PADDING.get();
                    y = (screenHeight - overlayHeight) / 2;
                }
                case CENTER -> {
                    x = (screenWidth - overlayWidth) / 2;
                    y = (screenHeight - overlayHeight) / 2;
                }
                case CENTER_RIGHT -> {
                    x = screenWidth - overlayWidth - Config.Client.X_PADDING.get();
                    y = (screenHeight - overlayHeight) / 2;
                }
                case BOTTOM_LEFT -> {
                    x = Config.Client.X_PADDING.get();
                    y = screenHeight - overlayHeight - Config.Client.Y_PADDING.get();
                }
                case BOTTOM_CENTER -> {
                    x = (screenWidth - overlayWidth) / 2;
                    y = screenHeight - overlayHeight - Config.Client.Y_PADDING.get();
                }
                case BOTTOM_RIGHT -> {
                    x = screenWidth - overlayWidth - Config.Client.X_PADDING.get();
                    y = screenHeight - overlayHeight - Config.Client.Y_PADDING.get();
                }
            }

            return new int[]{x, y};
        }
    }
}
