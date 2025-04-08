package com.raynna.blockroulette.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.raynna.blockroulette.Config;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetSpawn {

    private SetSpawn() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setspawn").executes(SetSpawn::run)
        );
    }

    private static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos pos = player.getOnPos();
        Config.Server.setSpawnPos(pos);
        player.sendSystemMessage(Component.literal("You have set your servers spawn location to: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()));
        return 1;
    }
}
