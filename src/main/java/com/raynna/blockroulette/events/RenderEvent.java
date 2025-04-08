package com.raynna.blockroulette.events;

import com.raynna.blockroulette.BlockRoulette;
import com.raynna.blockroulette.ui.TimerGui;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = BlockRoulette.MOD_ID, value = Dist.CLIENT)
public class RenderEvent {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRender(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        TimerGui.show(event.getGuiGraphics());
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(RenderEvent.class);
    }
}
