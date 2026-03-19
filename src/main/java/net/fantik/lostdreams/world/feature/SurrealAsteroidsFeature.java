package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SurrealAsteroidsFeature extends Feature<NoneFeatureConfiguration> {

    public SurrealAsteroidsFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private enum AsteroidType { RED, BLUE, YELLOW }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Астероиды на разных высотах — имитация пояса астероидов
        int asteroidY = -32 + random.nextInt(224);
        BlockPos base = new BlockPos(origin.getX(), asteroidY, origin.getZ());

        if (!level.getBlockState(base).isAir()) return false;

        AsteroidType type = AsteroidType.values()[random.nextInt(AsteroidType.values().length)];
        BlockState rock = switch (type) {
            case RED    -> ModBlocks.SURREAL_RED_ROCK.get().defaultBlockState();
            case BLUE   -> ModBlocks.SURREAL_BLUE_ROCK.get().defaultBlockState();
            case YELLOW -> ModBlocks.SURREAL_YELLOW_ROCK.get().defaultBlockState();
        };

        generateAsteroid(level, base, random, rock);
        return true;
    }

    private void generateAsteroid(WorldGenLevel level, BlockPos base,
                                  RandomSource random, BlockState rock) {
        int r = 3 + random.nextInt(9); // радиус 3-11
        boolean hasCave = random.nextFloat() < 0.35f && r >= 6;
        int caveR = (int)(r * 0.55f);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    // Идеальная сфера
                    if (x*x + y*y + z*z > r*r) continue;

                    // Пещера — сфера внутри
                    if (hasCave && x*x + y*y + z*z < caveR*caveR) continue;

                    level.setBlock(base.offset(x, y, z), rock, 2);
                }
            }
        }
    }
}