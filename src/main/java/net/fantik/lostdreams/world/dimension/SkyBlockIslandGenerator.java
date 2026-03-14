package net.fantik.lostdreams.world.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;

public class SkyBlockIslandGenerator {

    // Каждый игрок получает остров на своих координатах с шагом 200 блоков
    public static BlockPos getIslandOrigin(ServerPlayer player) {
        int index = Math.abs(player.getUUID().hashCode() % 10000);
        int gridX = (index % 100) * 200;
        int gridZ = (index / 100) * 200;
        return new BlockPos(gridX, 80, gridZ);
    }

    public static void generateIsland(ServerLevel level, BlockPos base) {
        // Проверяем — остров уже сгенерирован?
        if (!level.getBlockState(base).isAir()) return;

        var random = level.getRandom();
        int radiusX = 6 + random.nextInt(3);
        int radiusZ = 6 + random.nextInt(3);
        int radiusY = 3;

        // Тело острова — эллипсоид
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= 0; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    double ellipsoid = (double)(x * x) / (radiusX * radiusX)
                            + (double)(y * y) / (radiusY * radiusY)
                            + (double)(z * z) / (radiusZ * radiusZ);
                    if (ellipsoid > 1.0) continue;

                    BlockPos pos = base.offset(x, y, z);
                    if (y == 0) {
                        level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    } else if (y >= -2) {
                        level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 2);
                    } else {
                        level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
                    }
                }
            }
        }

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

// Наполняем сундук
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