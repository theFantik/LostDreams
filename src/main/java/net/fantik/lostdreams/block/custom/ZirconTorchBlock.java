package net.fantik.lostdreams.block.custom;

import net.fantik.lostdreams.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class ZirconTorchBlock extends TorchBlock {

    public ZirconTorchBlock(BlockBehaviour.Properties properties) {
        super(ParticleTypes.SOUL_FIRE_FLAME, properties);
    }

    @Override
    public void animateTick(BlockState state, Level level,
                            BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;


        level.addParticle(ModParticles.ZIRCON_FLAME.get(),
                x, y, z, 0, 0, 0);
        level.addParticle(ModParticles.ZIRCON_PARTICLES.get(),
                x + (random.nextDouble() - 0.5) * 0.2,
                y + random.nextDouble() * 0.1,
                z + (random.nextDouble() - 0.5) * 0.2,
                0, 0.01, 0);
    }
}
