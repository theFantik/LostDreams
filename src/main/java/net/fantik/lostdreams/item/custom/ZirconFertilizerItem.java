package net.fantik.lostdreams.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ZirconFertilizerItem extends Item {

    // Сколько раз применяется бонмил за 1 использование (обычный = 1)
    private static final int BONEMEAL_POWER = 2;

    public ZirconFertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof BonemealableBlock bonemealable)) {
            return InteractionResult.PASS;
        }

        if (!bonemealable.isValidBonemealTarget(level, pos, state)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Применяем бонмил BONEMEAL_POWER раз
            for (int i = 0; i < BONEMEAL_POWER; i++) {
                if (!bonemealable.isValidBonemealTarget(serverLevel, pos, state)) break;

                if (bonemealable.isBonemealSuccess(serverLevel,
                        serverLevel.random, pos, state)) {
                    bonemealable.performBonemeal(serverLevel,
                            serverLevel.random, pos, state);
                    state = serverLevel.getBlockState(pos); // обновляем стейт
                }
            }

            if (context.getPlayer() instanceof ServerPlayer player) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, pos, context.getItemInHand());
            }

            // Эффект частиц
            serverLevel.levelEvent(1505, pos, 0);

            level.playSound(null, pos,
                    SoundEvents.BONE_MEAL_USE,
                    SoundSource.BLOCKS, 1f, 1f);

            if (!context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
        } else {
            // Клиентские частицы
            for (int i = 0; i < 8; i++) {
                level.addParticle(
                        net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                        pos.getX() + level.random.nextDouble(),
                        pos.getY() + level.random.nextDouble(),
                        pos.getZ() + level.random.nextDouble(),
                        0, 0, 0);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
