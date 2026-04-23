package net.fantik.lostdreams.block.custom;

import com.mojang.serialization.MapCodec;
import net.fantik.lostdreams.block.entity.ModBlockEntities;
import net.fantik.lostdreams.block.entity.ZirconCampfireBlockEntity;
import net.fantik.lostdreams.entity.ModEntities;
import net.fantik.lostdreams.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class ZirconCampfireBlock extends CampfireBlock {

    public static final MapCodec<ZirconCampfireBlock> CODEC =
            simpleCodec(props -> new ZirconCampfireBlock(true, 3, props));

    private static final double REPEL_RADIUS = 8.0;
    private static final int REPEL_CHANCE = 20;

    public ZirconCampfireBlock(boolean spawnParticles, int fireDamage,
                               BlockBehaviour.Properties properties) {
        super(spawnParticles, fireDamage, properties);
    }

    @Override
    public MapCodec<CampfireBlock> codec() {
        return (MapCodec<CampfireBlock>)(MapCodec<?>) CODEC;
    }

    // -----------------------------------------------------------------------
    // BlockEntity
    // -----------------------------------------------------------------------

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZirconCampfireBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return state.getValue(LIT)
                    ? createTickerHelper(type, ModBlockEntities.ZIRCON_CAMPFIRE_BE.get(),
                    ZirconCampfireBlockEntity::particleTick)
                    : null;
        } else {
            return state.getValue(LIT)
                    ? createTickerHelper(type, ModBlockEntities.ZIRCON_CAMPFIRE_BE.get(),
                    ZirconCampfireBlockEntity::cookTick)
                    : createTickerHelper(type, ModBlockEntities.ZIRCON_CAMPFIRE_BE.get(),
                    ZirconCampfireBlockEntity::cooldownTick);
        }
    }

    // -----------------------------------------------------------------------
    // Урон от огня — 3 единицы
    // -----------------------------------------------------------------------

    @Override
    protected void entityInside(BlockState state, Level level,
                                BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().campfire(), 3.0f);
        }
        super.entityInside(state, level, pos, entity);
    }

    // -----------------------------------------------------------------------
    // Приготовление еды
    // -----------------------------------------------------------------------

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state,
                                              Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ZirconCampfireBlockEntity campfire) {
            ItemStack heldItem = player.getItemInHand(hand);
            Optional<RecipeHolder<CampfireCookingRecipe>> recipe =
                    campfire.getCookableRecipe(heldItem);
            if (recipe.isPresent()) {
                if (!level.isClientSide
                        && campfire.placeFood(player, heldItem,
                        recipe.get().value().getCookingTime())) {
                    player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return ItemInteractionResult.SUCCESS;
                }
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // -----------------------------------------------------------------------
    // Кастомные частицы
    // -----------------------------------------------------------------------

    @Override
    public void animateTick(BlockState state, Level level,
                            BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (!state.getValue(LIT)) return;

        if (random.nextInt(3) == 0) {
            level.addParticle(ModParticles.ZIRCON_FLAME.get(),
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                    pos.getY() + 0.3 + random.nextDouble() * 0.2,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.5,
                    0, 0.03, 0);
        }

        if (random.nextInt(6) == 0) {
            level.addParticle(ModParticles.ZIRCON_PARTICLES.get(),
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.015, 0);
        }
    }

    // -----------------------------------------------------------------------
    // Отпугивание мобов
    // -----------------------------------------------------------------------

    @Override
    public void randomTick(BlockState state,
                           net.minecraft.server.level.ServerLevel level,
                           BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (!state.getValue(LIT)) return;
        if (random.nextInt(REPEL_CHANCE) != 0) return;

        List<Mob> mobs = level.getEntitiesOfClass(Mob.class,
                new AABB(pos).inflate(REPEL_RADIUS), mob ->
                        mob.getType() == ModEntities.NULL_BUG.get()
                                || mob.getType() == ModEntities.LUCID_WASTE.get());

        for (Mob mob : mobs) {
            double dx = mob.getX() - (pos.getX() + 0.5);
            double dz = mob.getZ() - (pos.getZ() + 0.5);
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.01) { dx = 0.5; dz = 0.5; len = Math.sqrt(0.5); }
            mob.setDeltaMovement(mob.getDeltaMovement().add(
                    dx / len * 0.4, 0.1, dz / len * 0.4));
            mob.getNavigation().stop();
        }
    }
}