package com.raynna.blockroulette.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class Commands {


    private Commands() { throw new IllegalStateException("Utility class"); }

    public static void registerAll(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        SetSpawn.register(dispatcher);
    }
}
