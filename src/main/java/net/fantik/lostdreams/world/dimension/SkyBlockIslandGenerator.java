package net.fantik.lostdreams.world.dimension;

import net.fantik.lostdreams.util.IslandShapeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;

public class SkyBlockIslandGenerator {

    public static BlockPos getIslandOrigin(ServerPlayer player) {
        int index = Math.abs(player.getUUID().hashCode() % 10000);
        int gridX = (index % 100) * 200;
        int gridZ = (index / 100) * 200;
        return new BlockPos(gridX + 35, 80, gridZ + 35);
    }

    public static void generateIsland(ServerLevel level, BlockPos base) {
        generateIsland(level, base, false);
    }

    public static void generateIsland(ServerLevel level, BlockPos base, boolean force) {
        if (!force && !level.getBlockState(base).isAir()) return;

        var random = level.getRandom();
        int radiusX = 6 + random.nextInt(4);
        int radiusZ = 6 + random.nextInt(4);

        // Тело острова через IslandShapeUtil
        IslandShapeUtil.buildIsland(level, base, radiusX, radiusZ, random,
                Blocks.GRASS_BLOCK.defaultBlockState(),
                Blocks.DIRT.defaultBlockState(),
                Blocks.STONE.defaultBlockState());

        // Дерево
        BlockPos treeBase = base.above();
        int trunkHeight = 4 + random.nextInt(2);
        for (int y = 0; y < trunkHeight; y++) {
            level.setBlock(treeBase.above(y), Blocks.OAK_LOG.defaultBlockState(), 2);
        }
        BlockPos leafCenter = treeBase.above(trunkHeight - 1);
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2 && y <= 0) continue;
                    BlockPos lpos = leafCenter.offset(x, y, z);
                    if (level.getBlockState(lpos).isAir()) {
                        level.setBlock(lpos, Blocks.OAK_LEAVES.defaultBlockState()
                                .setValue(LeavesBlock.PERSISTENT, false), 2);
                    }
                }
            }
        }

        // Сундук
        BlockPos chestPos = base.offset(2, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);

        net.minecraft.world.level.block.entity.ChestBlockEntity chest =
                (net.minecraft.world.level.block.entity.ChestBlockEntity) level.getBlockEntity(chestPos);
        if (chest != null) {
            chest.setItem(0, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ICE, 2));
            chest.setItem(1, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LAVA_BUCKET, 1));
            chest.setItem(2, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.BREAD, 16));
            chest.setItem(3, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.OAK_SAPLING, 1));
        }
    }
}