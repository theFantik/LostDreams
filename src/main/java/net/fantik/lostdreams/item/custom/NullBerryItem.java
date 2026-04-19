package net.fantik.lostdreams.item.custom;

import net.fantik.lostdreams.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class NullBerryItem extends Item {

    public NullBerryItem(Properties properties) {
        super(properties.food(
                new FoodProperties.Builder()
                        .nutrition(2)
                        .saturationModifier(0.4f)
                        .build()
        ));
    }

    // -----------------------------------------------------------------------
    // Посадка куста правым кликом по земле
    // -----------------------------------------------------------------------

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();

        // Кликаем по верхней грани блока
        if (face != Direction.UP) return InteractionResult.PASS;

        BlockPos plantPos = pos.above();
        BlockState soilState = level.getBlockState(pos);
        BlockState airState = level.getBlockState(plantPos);

        // Проверяем что куст можно посадить здесь
        BlockState bushState = ModBlocks.NULL_BERRY_BUSH.get().defaultBlockState();
        if (!airState.isAir()) return InteractionResult.PASS;
        if (!bushState.canSurvive(level, plantPos)) return InteractionResult.PASS;

        if (!level.isClientSide) {
            level.setBlock(plantPos, bushState, 3);
            level.playSound(null, plantPos,
                    SoundEvents.CROP_PLANTED,
                    SoundSource.BLOCKS, 1.0f, 1.0f);

            Player player = context.getPlayer();
            if (player != null && !player.isCreative()) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // -----------------------------------------------------------------------
    // Еда — ночное зрение + плавное падение
    // -----------------------------------------------------------------------

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);

        if (!level.isClientSide) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, 600, 0, false, true, true));
            entity.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 600, 0, false, true, true));
        }

        return result;
    }
}