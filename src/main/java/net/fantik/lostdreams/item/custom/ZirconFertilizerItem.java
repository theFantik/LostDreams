package net.fantik.lostdreams.item.custom;

import net.fantik.lostdreams.particle.ModParticles;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CampfireBlock;
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
        BlockPos clickedPos = context.getClickedPos();
        BlockPos offsetPos = clickedPos.relative(context.getClickedFace());
        ItemStack stack = context.getItemInHand();

        boolean success = false;

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;

            for (int i = 0; i < BONEMEAL_POWER; i++) {

                // 🌱 1) Попытка применить обычный бонмил (пшеница, трава и тд)
                boolean applied = BoneMealItem.applyBonemeal(
                        stack, serverLevel, clickedPos, context.getPlayer()
                );

                if (applied) {
                    success = true;
                    continue;
                }

                // 🌊 2) Попытка вырастить водные растения (ламинария, морская трава, кораллы)
                BlockState state = level.getBlockState(clickedPos);
                boolean sturdyFace = state.isFaceSturdy(level, clickedPos, context.getClickedFace());

                if (sturdyFace) {
                    boolean waterGrow = BoneMealItem.growWaterPlant(
                            stack, serverLevel, offsetPos, context.getClickedFace()
                    );

                    if (waterGrow) {
                        success = true;
                    }
                }
            }

            // 🎉 эффекты как у ванили
            if (success) {
                serverLevel.levelEvent(1505, clickedPos, 15);
                level.playSound(null, clickedPos,
                        SoundEvents.BONE_MEAL_USE,
                        SoundSource.BLOCKS, 1f, 1f);

                if (context.getPlayer() instanceof ServerPlayer player) {
                    CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(player, clickedPos, stack);
                    player.swing(context.getHand(), true);
                }
            }
        }

        // Клиентские частицы
        if (success && level.isClientSide) {
            for (int i = 0; i < 8; i++) {
                level.addParticle(
                        ModParticles.ZIRCON_PARTICLES.get(),
                        clickedPos.getX() + level.random.nextDouble(),
                        clickedPos.getY() + level.random.nextDouble(),
                        clickedPos.getZ() + level.random.nextDouble(),
                        0, 0, 0);
            }
        }

        return success
                ? InteractionResult.sidedSuccess(level.isClientSide)
                : InteractionResult.PASS;
    }
}
