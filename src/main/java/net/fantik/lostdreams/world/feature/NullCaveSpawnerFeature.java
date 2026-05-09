package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.fantik.lostdreams.LostDreams;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class NullCaveSpawnerFeature extends Feature<NoneFeatureConfiguration> {

    private static final ResourceLocation TEMPLATE_ID =
            ResourceLocation.fromNamespaceAndPath("lostdreams", "underground/null_bug_spawner");

    public NullCaveSpawnerFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        // Ищем пол прямо под origin — спускаемся вниз
        BlockPos floorPos = null;
        for (int dy = 0; dy >= -16; dy--) {
            BlockPos check = origin.offset(0, dy, 0);
            BlockState here  = level.getBlockState(check);
            BlockState above = level.getBlockState(check.above());

            if (here.isSolid() && (above.isAir() || above.is(Blocks.CAVE_AIR))) {
                floorPos = check;
                break;
            }
        }

        // Если не нашли пол под — ищем над origin
        if (floorPos == null) {
            for (int dy = 1; dy <= 16; dy++) {
                BlockPos check = origin.offset(0, dy, 0);
                BlockState here  = level.getBlockState(check);
                BlockState above = level.getBlockState(check.above());

                if (here.isSolid() && (above.isAir() || above.is(Blocks.CAVE_AIR))) {
                    floorPos = check;
                    break;
                }
            }
        }

        if (floorPos == null) return false;

        BlockPos placePos = floorPos.above();

        // Минимум 2 блока высоты
        for (int i = 0; i < 2; i++) {
            BlockState state = level.getBlockState(placePos.above(i));
            if (!state.isAir() && !state.is(Blocks.CAVE_AIR)) return false;
        }

        // Проверяем что рядом нет другого спавнера
        if (hasNearbySpawner(level, floorPos, 20)) return false;

        return placeStructure(level, placePos, random);
    }

    /**
     * Ищет пол пещеры — твёрдый блок с воздухом сверху.
     * Ищет в нескольких случайных точках вокруг origin.
     */
    private BlockPos findCaveFloor(WorldGenLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 20; attempt++) {
            int dx = random.nextInt(9) - 4;
            int dz = random.nextInt(9) - 4;

            // Сканируем столбец сверху вниз
            for (int dy = 4; dy >= -4; dy--) {
                BlockPos pos = origin.offset(dx, dy, dz);

                if (pos.getY() < 5) continue;

                BlockState above = level.getBlockState(pos.above());
                BlockState here  = level.getBlockState(pos);

                boolean hereIsSolid = here.isSolid();
                boolean aboveIsAir  = above.isAir() || above.is(Blocks.CAVE_AIR);

                if (hereIsSolid && aboveIsAir) {
                    return pos; // возвращаем пол
                }
            }
        }
        return null;
    }

    /**
     * Проверяет что над placePos есть минимум 3 воздушных блока.
     */
    private boolean hasEnoughSpace(WorldGenLevel level, BlockPos pos) {
        for (int i = 0; i < 3; i++) {
            BlockState state = level.getBlockState(pos.above(i));
            if (!state.isAir() && !state.is(Blocks.CAVE_AIR)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Пытается разместить NBT структуру.
     * Если шаблон не найден — размещает спавнер вручную.
     */
    private boolean placeStructure(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // WorldGenLevel.getLevel() возвращает ServerLevel всегда
        ServerLevel serverLevel = level.getLevel();
        StructureTemplateManager manager = serverLevel.getStructureManager();
        Optional<StructureTemplate> templateOpt = manager.get(TEMPLATE_ID);

        if (templateOpt.isPresent()) {
            return placeNbt(level, pos, templateOpt.get(), random); // передаём level, не serverLevel
        }

        LostDreams.LOGGER.warn("NullCaveSpawner: template {} not found", TEMPLATE_ID);
        return placeManual(level, pos, random);
    }

    private boolean placeNbt(WorldGenLevel level, BlockPos pos,
                             StructureTemplate template, RandomSource random) {
        int offX = template.getSize().getX() / 2;
        int offZ = template.getSize().getZ() / 2;
        BlockPos placementPos = pos.offset(-offX, 0, -offZ);
        StructurePlaceSettings settings = new StructurePlaceSettings();
        template.placeInWorld(level, placementPos, placementPos, settings, random, 2);
        return true;
    }

    private boolean hasNearbySpawner(WorldGenLevel level, BlockPos pos, int radius) {
        for (int dx = -radius; dx <= radius; dx += 4) {
            for (int dz = -radius; dz <= radius; dz += 4) {
                for (int dy = -8; dy <= 8; dy += 4) {
                    BlockPos check = pos.offset(dx, dy, dz);
                    if (level.getBlockState(check).is(Blocks.SPAWNER)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Ручное размещение: платформа + спавнер + декор.
     */
    private boolean placeManual(WorldGenLevel level, BlockPos pos, RandomSource random) {
        // Платформа 3x3 под спавнером
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos floorBlock = pos.below().offset(dx, 0, dz);
                if (!level.getBlockState(floorBlock).isSolid()) {
                    level.setBlock(floorBlock,
                            Blocks.DEEPSLATE.defaultBlockState(), 2);
                }
            }
        }

        // Очищаем пространство 3x3x3 над платформой
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos clearPos = pos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(clearPos);
                    if (!state.isAir() && !state.is(Blocks.CAVE_AIR)) {
                        level.setBlock(clearPos,
                                Blocks.CAVE_AIR.defaultBlockState(), 2);
                    }
                }
            }
        }

        // Спавнер в центре
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);

        // Декор: факелы на стенах
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos wall  = pos.relative(dir, 2);
            BlockPos torch = pos.relative(dir, 1);

            if (level.getBlockState(wall).isSolid()
                    && level.getBlockState(torch).isAir()) {
                level.setBlock(torch,
                        Blocks.SOUL_TORCH.defaultBlockState(), 2);
            }
        }

        // Паутина по краям
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                    BlockPos webPos = pos.offset(dx, 0, dz);
                    if (level.getBlockState(webPos).isAir()
                            && random.nextFloat() < 0.35f) {
                        level.setBlock(webPos,
                                Blocks.COBWEB.defaultBlockState(), 2);
                    }
                }
            }
        }

        return true;
    }
}