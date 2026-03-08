package net.fantik.lostdreams.world.feature;

import com.mojang.serialization.Codec;
import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VoidHoleFeature extends Feature<NoneFeatureConfiguration> {

    public VoidHoleFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        Random random = new Random(context.random().nextLong());

        int x = origin.getX();
        int z = origin.getZ();

        boolean wide = random.nextBoolean();
        int minY = level.getMinBuildHeight();
        int startY = origin.getY();
        int holeSize = wide ? 2 : 1;

        List<BlockPos> nullGroundPositions = new ArrayList<>();

        for (int y = startY; y >= minY; y--) {
            List<BlockPos> placed = fillWaterAround(level, x, y, z, holeSize);
            nullGroundPositions.addAll(placed);

            setAir(level, x, y, z);
            if (wide) {
                setAir(level, x + 1, y, z);
                setAir(level, x, y, z + 1);
                setAir(level, x + 1, y, z + 1);
            }
        }

        // Ставим табличку на самый верхний null_ground
        if (!nullGroundPositions.isEmpty()) {
            BlockPos topPos = nullGroundPositions.stream()
                    .max((a, b) -> Integer.compare(a.getY(), b.getY()))
                    .orElse(null);

            if (topPos != null) {
                placeSign(level, topPos.above());
            }
        }

        return true;
    }

    private List<BlockPos> fillWaterAround(WorldGenLevel level, int x, int y, int z, int holeSize) {
        BlockState nullGround = ModBlocks.NULL_GROUND.get().defaultBlockState();
        List<BlockPos> placed = new ArrayList<>();

        int minX = x - 1;
        int maxX = x + holeSize;
        int minZ = z - 1;
        int maxZ = z + holeSize;

        for (int bx = minX; bx <= maxX; bx++) {
            for (int bz = minZ; bz <= maxZ; bz++) {
                boolean insideHole = bx >= x && bx < x + holeSize
                        && bz >= z && bz < z + holeSize;
                if (insideHole) continue;

                BlockPos pos = new BlockPos(bx, y, bz);
                if (level.getBlockState(pos).getBlock() == Blocks.WATER) {
                    level.setBlock(pos, nullGround, 2);
                    placed.add(pos);
                }
            }
        }

        return placed;
    }

    private void placeSign(WorldGenLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) return;

        BlockState signState = Blocks.OAK_SIGN.defaultBlockState()
                .setValue(StandingSignBlock.ROTATION, 0);

        // Ставим блок таблички
        level.setBlock(pos, signState, 2);

        // Пишем текст через NBT — единственный безопасный способ во время worldgen
        // (BlockEntity.level == null на этом этапе, поэтому setText() крашится)
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            CompoundTag nbt = be.saveWithoutMetadata(level.registryAccess());

            // Формат таблички в 1.21: front_text.messages — массив из 4 строк JSON
            CompoundTag frontText = new CompoundTag();
            net.minecraft.nbt.ListTag messages = new net.minecraft.nbt.ListTag();
            messages.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"\"}"));
            messages.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"EXIT\"}"));
            messages.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"\"}"));
            messages.add(net.minecraft.nbt.StringTag.valueOf("{\"text\":\"\"}"));
            frontText.put("messages", messages);
            frontText.putBoolean("has_glowing_text", false);
            frontText.putString("color", "black");
            nbt.put("front_text", frontText);

            be.loadWithComponents(nbt, level.registryAccess());
        }
    }

    private void setAir(WorldGenLevel level, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}