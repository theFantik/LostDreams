package net.fantik.lostdreams.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class AfterDreamingEffect extends MobEffect {

    public AfterDreamingEffect() {
        super(MobEffectCategory.NEUTRAL, 0x2a0a4a); // тёмно-фиолетовый цвет
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // Логика на сервере если нужна (замедление, дезориентация)
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
