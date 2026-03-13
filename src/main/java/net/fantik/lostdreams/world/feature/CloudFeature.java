package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CloudFeature extends Feature<NoneFeatureConfiguration> {

    public CloudFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Высота облаков
        int cloudY = 200 + random.nextInt(30);
        BlockPos cloudOrigin = new BlockPos(origin.getX(), cloudY, origin.getZ());

        int radiusX = 2 + random.nextInt(3); // 2-4
        int radiusY = 1;                      // плоские как настоящие облака
        int radiusZ = 2 + random.nextInt(3); // 2-4

        // Генерируем эллипсоид — плоское облако
        for (int x = -radiusX; x <= radiusX; x++) {
            for (int y = -radiusY; y <= radiusY; y++) {
                for (int z = -radiusZ; z <= radiusZ; z++) {
                    // Проверка эллипсоида
                    double ellipsoid = (double)(x * x) / (radiusX * radiusX)
                            + (double)(y * y) / (radiusY * radiusY + 0.5)
                            + (double)(z * z) / (radiusZ * radiusZ);
                    if (ellipsoid > 1.0) continue;

                    // Рваные края — пропускаем блоки на краях с шансом
                    double edgeFactor = ellipsoid;
                    if (edgeFactor > 0.6 && random.nextFloat() < (float)(edgeFactor - 0.6) * 2.5f) continue;

                    BlockPos pos = cloudOrigin.offset(x, y, z);
                    if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.WHITE_CONCRETE.defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }
}