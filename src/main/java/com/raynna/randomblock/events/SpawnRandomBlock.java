package com.raynna.randomblock.events;

import com.raynna.randomblock.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;

public class SpawnRandomBlock {

    private static final Random RANDOM = new Random();

    private static final BlockPos DEFAULT_POS = new BlockPos(6, 67, 3);

    private static final int SPAWN_RADIUS = 3;

    private static long lastBlockSpawnTime = 0;
    private static long lastItemSpawnTime = 0;
    private static ItemEntity lastItemEntity = null;

    @SubscribeEvent
    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
            if (event.getLevel().players().isEmpty()) return;
        if (Config.Server.SPAWN_BLOCK_MODE.get() != Config.Server.SpawnBlockMode.OFF) {
            checkAndSpawnBlock(serverLevel);
        }
        if (Config.Server.SPAWN_ITEM_MODE.get() != Config.Server.SpawnItemMode.OFF) {
            checkAndSpawnItem(serverLevel);
        }
    }

    private static void checkAndSpawnBlock(ServerLevel level) {
        long currentTime = level.getGameTime();
        long spawnInterval = Config.Server.SPAWN_BLOCK_TIMER.get() * 20L;
        long timeSinceLastSpawn = currentTime - lastBlockSpawnTime;
        if (lastBlockSpawnTime == 0L) {
            lastBlockSpawnTime = currentTime;
            return;
        }
        if (timeSinceLastSpawn < spawnInterval) {
            logTimeRemaining(spawnInterval - timeSinceLastSpawn);
            return;
        }

        spawnNewBlock(level);
    }

    private static void checkAndSpawnItem(ServerLevel level) {
        long currentTime = level.getGameTime();
        long spawnInterval = Config.Server.SPAWN_ITEM_TIMER.get() * 20L;
        long timeSinceLastSpawn = currentTime - lastItemSpawnTime;
        if (lastItemSpawnTime == 0L) {
            lastItemSpawnTime = currentTime;
            return;
        }
        if (timeSinceLastSpawn < spawnInterval) {
            logTimeRemainingItem(spawnInterval - timeSinceLastSpawn);
            return;
        }

        spawnNewItem(level);
    }

    private static BlockPos findSafeSpawnPosition(Level level, BlockPos centerPos) {
        for (int i = 0; i < 10; i++) {
            int x = centerPos.getX() + RANDOM.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS;
            int y = Math.min(Math.max(centerPos.getY() + RANDOM.nextInt(5) - 2, level.getMinBuildHeight()), level.getMaxBuildHeight());
            int z = centerPos.getZ() + RANDOM.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS;

            BlockPos testPos = new BlockPos(x, y, z);

            if (testPos.equals(centerPos)) continue;

            BlockState state = level.getBlockState(testPos);
            if (state.isAir() || state.canBeReplaced()) {
                return testPos;
            }
        }
        return null;
    }

    private static void logTimeRemainingItem(long ticksRemaining) {
        long secondsRemaining = Math.max(ticksRemaining / 20, 0);
    }

    private static void logTimeRemaining(long ticksRemaining) {
        long secondsRemaining = Math.max(ticksRemaining / 20, 0);
    }

    private static void spawnNewBlock(ServerLevel level) {
        List<Block> validBlocks = getValidBlocks(level);
        if (validBlocks.isEmpty()) return;

        Config.Server.SpawnBlockMode mode = Config.Server.SPAWN_BLOCK_MODE.get();

        Block randomBlock = validBlocks.get(RANDOM.nextInt(validBlocks.size()));
        BlockState blockState = randomBlock.defaultBlockState();

        if (mode == Config.Server.SpawnBlockMode.ONE_IN_WORLD) {
            handleSingleBlockSpawn(level, randomBlock, blockState);
        } else {
            handleMultipleBlockSpawn(level, randomBlock, blockState);
        }
        lastBlockSpawnTime = level.getGameTime();
    }

    private static void spawnNewItem(ServerLevel level) {
        List<Item> validItems = getValidItems();
        if (validItems.isEmpty()) return;

        Config.Server.SpawnItemMode mode = Config.Server.SPAWN_ITEM_MODE.get();
        Item randomItem = validItems.get(RANDOM.nextInt(validItems.size()));
        ItemStack itemStack = new ItemStack(randomItem);

        if (mode == Config.Server.SpawnItemMode.ONE_IN_WORLD) {
            handleSingleItemSpawn(level, randomItem, itemStack);
        } else {
            handleMultipleItemSpawn(level, randomItem, itemStack);
        }

        lastItemSpawnTime = level.getGameTime();
    }


    private static List<Item> getValidItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> !(item instanceof ArmorItem))
                .filter(item -> !(item instanceof ElytraItem))
                .filter(item -> !(item instanceof ShieldItem))
                .filter(item -> !(item instanceof TieredItem))
                .filter(item -> !(item instanceof BowItem || item instanceof CrossbowItem))
                .filter(item -> !(item instanceof TridentItem))
                .filter(item -> !(item instanceof FishingRodItem))
                .filter(item -> !(item instanceof ProjectileItem))
                .filter(item -> !(item instanceof BlockItem))
                .toList();
    }

    private static List<Block> getValidBlocks(Level level) {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block.defaultBlockState().canOcclude())
                .filter(block -> !block.defaultBlockState().getCollisionShape(level, BlockPos.ZERO).isEmpty())
                .filter(block -> !block.defaultBlockState().isAir())
                .filter(block -> block.defaultBlockState().getFluidState().isEmpty())
                .toList();
    }

    private static void handleSingleBlockSpawn(ServerLevel level, Block block, BlockState state) {
        BlockPos spawnPos = level.players().stream().findFirst().map(Entity::blockPosition).orElse(DEFAULT_POS);

        if (lastPlacedBlock != null) {
            if (!level.isEmptyBlock(lastPlacedBlock.pos) &&
                    level.getBlockState(lastPlacedBlock.pos).getBlock() == lastPlacedBlock.block) {
                level.setBlock(lastPlacedBlock.pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        level.setBlock(spawnPos, state, 3);
        lastPlacedBlock = new PlacedBlock(spawnPos, block);
        broadcastSpawnMessage(level, block, "§6A " + block.getName().getString() +
                " has appeared in spawn. It will last for 1 minute.");
    }

    private static void handleMultipleBlockSpawn(ServerLevel level, Block block, BlockState state) {
        for (ServerPlayer player : level.players()) {
            if (player == null) continue;

            // Remove old block if it still exists
            PlacedBlock oldBlock = placedBlocks.get(player);
            if (oldBlock != null && level.getBlockState(oldBlock.pos).getBlock() == oldBlock.block) {
                level.setBlock(oldBlock.pos, Blocks.AIR.defaultBlockState(), 3);
            }

            // Pick new block and spawn position
            Block randomBlock = getValidBlocks(level).get(RANDOM.nextInt(getValidBlocks(level).size()));
            BlockState randomState = randomBlock.defaultBlockState();
            BlockPos spawnPos = findSafeSpawnPosition(level, player.blockPosition());

            if (spawnPos != null) {
                level.setBlock(spawnPos, randomState, 3);
                placedBlocks.put(player, new PlacedBlock(spawnPos, randomBlock));

                player.sendSystemMessage(Component.literal("§6A " + randomBlock.getName().getString() +
                        " has appeared near you. It will last for 1 minute."));
            }
        }
    }

    private static void broadcastSpawnMessage(ServerLevel level, Block block, String message) {
        Component component = Component.literal(message);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(component);
        }
    }

    private static void handleSingleItemSpawn(ServerLevel level, Item item, ItemStack itemStack) {
        BlockPos spawnPos = level.players().stream().findFirst().map(Entity::blockPosition).orElse(DEFAULT_POS);

        if (lastItemEntity == null || !lastItemEntity.isAlive()) {
            ItemEntity itemEntity = new ItemEntity(level, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, itemStack);
            level.addFreshEntity(itemEntity);
            lastItemEntity = itemEntity;

            broadcastItemSpawnMessage(level, item, "A mysterious " + item.getName(itemStack).getString() +
                    " has appeared at spawn!");
        }
    }

    private static void handleMultipleItemSpawn(ServerLevel level, Item item, ItemStack itemStack) {
        for (ServerPlayer player : level.players()) {
            if (player == null) continue;

            Item randomItem = getValidItems().get(RANDOM.nextInt(getValidItems().size()));
            ItemStack playerItemStack = new ItemStack(randomItem);

            BlockPos spawnPos = findSafeSpawnPosition(level, player.blockPosition());
            if (spawnPos != null) {
                ItemEntity itemEntity = new ItemEntity(level,
                        spawnPos.getX() + 0.5,
                        spawnPos.getY() + 0.5,
                        spawnPos.getZ() + 0.5,
                        playerItemStack);

                level.addFreshEntity(itemEntity);
                player.sendSystemMessage(Component.literal("A mysterious " +
                        randomItem.getName(playerItemStack).getString() +
                        " has appeared near you!"));
            }
        }
    }

    private static void broadcastItemSpawnMessage(ServerLevel level, Item item, String message) {
        Component component = Component.literal(message).withStyle(ChatFormatting.GREEN);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(component);
        }
    }

    private static PlacedBlock lastPlacedBlock = null;
    private static final Map<ServerPlayer, PlacedBlock> placedBlocks = new HashMap<>();

    private static class PlacedBlock {
        BlockPos pos;
        Block block;

        PlacedBlock(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
        }
    }


    public static void register() {
        NeoForge.EVENT_BUS.register(SpawnRandomBlock.class);
    }
}
