package net.fantik.lostdreams.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class ZirconWallTorchBlock extends WallTorchBlock {

    private final Supplier<SimpleParticleType> particleSupplier;

    public ZirconWallTorchBlock(Supplier<SimpleParticleType> particleSupplier, Properties props) {
        super(null, props); // временно null
        this.particleSupplier = particleSupplier;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction direction = state.getValue(FACING);
        Direction opposite = direction.getOpposite();

        double x = pos.getX() + 0.5 + 0.27 * opposite.getStepX();
        double y = pos.getY() + 0.7 + 0.22 * opposite.getStepY();
        double z = pos.getZ() + 0.5 + 0.27 * opposite.getStepZ();
        level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,x,y,z,0,0,0);
        level.addParticle(particleSupplier.get(), x + (random.nextDouble()-0.5) * 0.2, y + random.nextDouble() * 0.1, z + (random.nextDouble() - 0.5) * 0.2, 0, 0, 0);
    }
}