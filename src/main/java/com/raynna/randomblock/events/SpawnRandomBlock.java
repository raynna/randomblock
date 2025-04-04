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
    private static PlacedBlock lastPlacedBlock = null;
    private static final Map<ServerPlayer, PlacedBlock> placedBlocks = new HashMap<>();

    @SubscribeEvent
    private static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;
        if (serverLevel.players().isEmpty()) return;

        if (Config.Server.SPAWN_BLOCK_MODE.get() != Config.Server.SpawnBlockMode.OFF) {
            checkAndSpawnBlock(serverLevel);
        }
        if (Config.Server.SPAWN_ITEM_MODE.get() != Config.Server.SpawnItemMode.OFF) {
            checkAndSpawnItem(serverLevel);
        }
    }

    private static void checkAndSpawnBlock(ServerLevel level) {
        long currentTime = level.getGameTime();
        long interval = Config.Server.SPAWN_BLOCK_TIMER.get() * 20L;

        if (lastBlockSpawnTime == 0L) {
            lastBlockSpawnTime = currentTime;
            return;
        }
        if ((currentTime - lastBlockSpawnTime) < interval) return;

        spawnNewBlock(level);
        lastBlockSpawnTime = currentTime;
    }

    private static void checkAndSpawnItem(ServerLevel level) {
        long currentTime = level.getGameTime();
        long interval = Config.Server.SPAWN_ITEM_TIMER.get() * 20L;

        if (lastItemSpawnTime == 0L) {
            lastItemSpawnTime = currentTime;
            return;
        }
        if ((currentTime - lastItemSpawnTime) < interval) return;

        spawnNewItem(level);
        lastItemSpawnTime = currentTime;
    }

    private static void spawnNewBlock(ServerLevel level) {
        List<Block> validBlocks = getValidBlocks(level);
        if (validBlocks.isEmpty()) {
            System.err.println("[RandomBlock] No valid blocks found to spawn.");
            return;
        }

        Config.Server.SpawnBlockMode mode = Config.Server.SPAWN_BLOCK_MODE.get();
        Block randomBlock = validBlocks.get(RANDOM.nextInt(validBlocks.size()));
        BlockState blockState = randomBlock.defaultBlockState();

        if (mode == Config.Server.SpawnBlockMode.ONE_IN_WORLD) {
            handleSingleBlockSpawn(level, randomBlock, blockState);
        } else {
            handleMultipleBlockSpawn(level, validBlocks);
        }
    }

    private static void spawnNewItem(ServerLevel level) {
        List<Item> validItems = getValidItems();
        if (validItems.isEmpty()) {
            System.err.println("[RandomBlock] No valid items found to spawn.");
            return;
        }

        Config.Server.SpawnItemMode mode = Config.Server.SPAWN_ITEM_MODE.get();
        Item randomItem = validItems.get(RANDOM.nextInt(validItems.size()));
        ItemStack itemStack = new ItemStack(randomItem);

        if (mode == Config.Server.SpawnItemMode.ONE_IN_WORLD) {
            handleSingleItemSpawn(level, randomItem, itemStack);
        } else {
            handleMultipleItemSpawn(level, validItems);
        }
    }

    private static void handleSingleBlockSpawn(ServerLevel level, Block block, BlockState state) {
        BlockPos spawnPos = level.players().stream().findFirst().map(Entity::blockPosition).orElse(DEFAULT_POS);

        if (lastPlacedBlock != null &&
                !level.isEmptyBlock(lastPlacedBlock.pos) &&
                level.getBlockState(lastPlacedBlock.pos).getBlock() == lastPlacedBlock.block) {
            level.setBlock(lastPlacedBlock.pos, Blocks.AIR.defaultBlockState(), 3);
        }

        level.setBlock(spawnPos, state, 3);
        lastPlacedBlock = new PlacedBlock(spawnPos, block);

        broadcastMessage(level, "§6A " + block.getName().getString() + " has appeared at spawn!");
    }

    private static void handleMultipleBlockSpawn(ServerLevel level, List<Block> validBlocks) {
        for (ServerPlayer player : level.players()) {
            PlacedBlock oldBlock = placedBlocks.get(player);
            if (oldBlock != null && level.getBlockState(oldBlock.pos).getBlock() == oldBlock.block) {
                level.setBlock(oldBlock.pos, Blocks.AIR.defaultBlockState(), 3);
            }

            Block randomBlock = validBlocks.get(RANDOM.nextInt(validBlocks.size()));
            BlockState state = randomBlock.defaultBlockState();
            BlockPos spawnPos = findSafeSpawnPosition(level, player.blockPosition());

            if (spawnPos != null) {
                level.setBlock(spawnPos, state, 3);
                placedBlocks.put(player, new PlacedBlock(spawnPos, randomBlock));
                player.sendSystemMessage(Component.literal("§6A " + randomBlock.getName().getString() +
                        " has appeared near you."));
            }
        }
    }

    private static void handleSingleItemSpawn(ServerLevel level, Item item, ItemStack stack) {
        BlockPos spawnPos = level.players().stream().findFirst().map(Entity::blockPosition).orElse(DEFAULT_POS);

        if (lastItemEntity == null || !lastItemEntity.isAlive()) {
            ItemEntity itemEntity = new ItemEntity(level, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, stack);
            level.addFreshEntity(itemEntity);
            lastItemEntity = itemEntity;

            broadcastMessage(level, "§aA mysterious " + item.getName(stack).getString() + " has appeared at spawn!");
        }
    }

    private static void handleMultipleItemSpawn(ServerLevel level, List<Item> validItems) {
        for (ServerPlayer player : level.players()) {
            Item randomItem = validItems.get(RANDOM.nextInt(validItems.size()));
            ItemStack stack = new ItemStack(randomItem);
            BlockPos spawnPos = findSafeSpawnPosition(level, player.blockPosition());

            if (spawnPos != null) {
                ItemEntity itemEntity = new ItemEntity(level,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, stack);
                level.addFreshEntity(itemEntity);

                player.sendSystemMessage(Component.literal("§aA mysterious " +
                        randomItem.getName(stack).getString() + " has appeared near you!"));
            }
        }
    }

    private static List<Block> getValidBlocks(Level level) {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> block.defaultBlockState().canOcclude())
                .filter(block -> !block.defaultBlockState().getCollisionShape(level, BlockPos.ZERO).isEmpty())
                .filter(block -> !block.defaultBlockState().isAir())
                .filter(block -> block.defaultBlockState().getFluidState().isEmpty())
                .toList();
    }

    private static List<Item> getValidItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> !(item instanceof ArmorItem || item instanceof ElytraItem ||
                        item instanceof ShieldItem || item instanceof TieredItem ||
                        item instanceof BowItem || item instanceof CrossbowItem ||
                        item instanceof TridentItem || item instanceof FishingRodItem ||
                        item instanceof ProjectileItem || item instanceof BlockItem))
                .toList();
    }

    private static BlockPos findSafeSpawnPosition(Level level, BlockPos center) {
        for (int i = 0; i < 10; i++) {
            int x = center.getX() + RANDOM.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS;
            int y = Math.min(Math.max(center.getY() + RANDOM.nextInt(5) - 2, level.getMinBuildHeight()), level.getMaxBuildHeight());
            int z = center.getZ() + RANDOM.nextInt(SPAWN_RADIUS * 2 + 1) - SPAWN_RADIUS;
            BlockPos test = new BlockPos(x, y, z);

            if (!test.equals(center) && (level.getBlockState(test).isAir() || level.getBlockState(test).canBeReplaced())) {
                return test;
            }
        }
        return null;
    }

    private static void broadcastMessage(ServerLevel level, String msg) {
        Component comp = Component.literal(msg).withStyle(ChatFormatting.YELLOW);
        for (ServerPlayer player : level.players()) {
            player.sendSystemMessage(comp);
        }
    }

    private static final class PlacedBlock {
        final BlockPos pos;
        final Block block;

        PlacedBlock(BlockPos pos, Block block) {
            this.pos = pos;
            this.block = block;
        }
    }

    public static void register() {
        NeoForge.EVENT_BUS.register(SpawnRandomBlock.class);
    }
}
