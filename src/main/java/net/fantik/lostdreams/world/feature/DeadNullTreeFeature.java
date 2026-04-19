package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.fantik.lostdreams.block.ModBlocks;

public class DeadNullTreeFeature extends Feature<NoneFeatureConfiguration> {

    public DeadNullTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Высота ствола: 4-7 блоков
        int trunkHeight = 4 + random.nextInt(4);

        // Проверяем, что можно разместить
        if (!level.getBlockState(origin.below()).is(ModBlocks.NULL_GROUND.get())
                && !level.getBlockState(origin.below()).is(ModBlocks.NULL_STONE.get())) {
            return false;
        }

        BlockState logState = ModBlocks.NULL_LOG.get().defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, net.minecraft.core.Direction.Axis.Y);

        // Ставим ствол
        for (int i = 0; i < trunkHeight; i++) {
            BlockPos pos = origin.above(i);
            if (level.isEmptyBlock(pos)) {
                level.setBlock(pos, logState, 2);
            }
        }

        // Ветки (2-4 штуки)
        int branchCount = 2 + random.nextInt(3);
        for (int b = 0; b < branchCount; b++) {
            // Ветка начинается на случайной высоте (верхняя половина ствола)
            int branchY = trunkHeight / 2 + random.nextInt(trunkHeight / 2);
            BlockPos branchBase = origin.above(branchY);

            // Направление ветки
            int dx = random.nextInt(3) - 1; // -1, 0, 1
            int dz = random.nextInt(3) - 1;
            if (dx == 0 && dz == 0) dx = 1; // не вверх

            // Длина ветки: 1-3 блока
            int branchLen = 1 + random.nextInt(3);

            // Ось лога для ветки
            net.minecraft.core.Direction.Axis branchAxis;
            if (Math.abs(dx) >= Math.abs(dz)) {
                branchAxis = net.minecraft.core.Direction.Axis.X;
            } else {
                branchAxis = net.minecraft.core.Direction.Axis.Z;
            }

            BlockState branchState = ModBlocks.NULL_LOG.get().defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS, branchAxis);

            for (int l = 1; l <= branchLen; l++) {
                BlockPos branchPos = branchBase.offset(dx * l, 0, dz * l);
                if (level.isEmptyBlock(branchPos)) {
                    level.setBlock(branchPos, branchState, 2);
                }
                // Иногда ветка идёт чуть вверх
                if (l == branchLen && random.nextBoolean()) {
                    BlockPos tipPos = branchPos.above();
                    if (level.isEmptyBlock(tipPos)) {
                        level.setBlock(tipPos,
                                ModBlocks.NULL_LOG.get().defaultBlockState()
                                        .setValue(RotatedPillarBlock.AXIS,
                                                net.minecraft.core.Direction.Axis.Y), 2);
                    }
                }
            }
        }

        return true;
    }
}
