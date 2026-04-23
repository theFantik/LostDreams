package net.fantik.lostdreams.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class ZirconParticles extends TextureSheetParticle {

    protected ZirconParticles(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, float gravity, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.x = x;
        this.friction = 0.96F;
        this.y = y;
        this.z = z;
        this.gravity = gravity;

        this.quadSize = 0.1F * (this.random.nextFloat() * 0.2F + 0.5F);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
        this.lifetime = (int)(Math.random() * 10.0) + 40;
        this.setSpriteFromAge(spriteSet);
    }

    /**
     * @return
     */
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;

        }


        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new ZirconParticles(clientLevel, pX, pY, pZ, this.spriteSet, -0.1F, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}
