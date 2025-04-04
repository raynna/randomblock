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

        long currentTime = mc.level.getGameTime();

        // Block timer
        long blockInterval = Config.Server.SPAWN_BLOCK_TIMER.get() * 20L;
        long timeSinceLastBlock = currentTime - SpawnRandomBlock.getLastBlockSpawnTime();
        long blockRemaining = Mth.clamp(blockInterval - timeSinceLastBlock, 0, blockInterval) / 20;

        // Item timer
        long itemInterval = Config.Server.SPAWN_ITEM_TIMER.get() * 20L;
        long timeSinceLastItem = currentTime - SpawnRandomBlock.getLastItemSpawnTime();
        long itemRemaining = Mth.clamp(itemInterval - timeSinceLastItem, 0, itemInterval) / 20;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = 10;
        int y = screenWidth / 8;

        graphics.drawString(mc.font, Component.literal("§6Next Block Spawn: §f" + blockRemaining + "s"), x, y, 0xFFFFFF);
        graphics.drawString(mc.font, Component.literal("§aNext Item Spawn: §f" + itemRemaining + "s"), x, y + 10, 0xFFFFFF);
    }
}
