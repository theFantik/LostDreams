package net.fantik.lostdreams.block.entity;

import net.fantik.lostdreams.particle.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public class ZirconCampfireBlockEntity extends BlockEntity implements Clearable {

    private static final int BURN_COOL_SPEED = 2;
    private static final float COOK_SPEED_MULTIPLIER = 1.5f;

    private final NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private final int[] cookingProgress = new int[4];
    private final int[] cookingTime = new int[4];
    private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck;

    public ZirconCampfireBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.ZIRCON_CAMPFIRE_BE.get(), pos, blockState);
        this.quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
    }

    // -----------------------------------------------------------------------
    // Cook tick
    // -----------------------------------------------------------------------

    public static void cookTick(Level level, BlockPos pos, BlockState state,
                                ZirconCampfireBlockEntity be) {
        boolean changed = false;
        for (int i = 0; i < be.items.size(); i++) {
            ItemStack stack = be.items.get(i);
            if (stack.isEmpty()) continue;
            changed = true;
            be.cookingProgress[i]++;
            if (be.cookingProgress[i] >= be.cookingTime[i]) {
                SingleRecipeInput input = new SingleRecipeInput(stack);
                ItemStack result = be.quickCheck
                        .getRecipeFor(input, level)
                        .map(r -> r.value().assemble(input, level.registryAccess()))
                        .orElse(stack);
                if (result.isItemEnabled(level.enabledFeatures())) {
                    Containers.dropItemStack(level,
                            pos.getX(), pos.getY(), pos.getZ(), result);
                    be.items.set(i, ItemStack.EMPTY);
                    level.sendBlockUpdated(pos, state, state, 3);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos,
                            GameEvent.Context.of(state));
                }
            }
        }
        if (changed) setChanged(level, pos, state);
    }

    // -----------------------------------------------------------------------
    // Cooldown tick
    // -----------------------------------------------------------------------

    public static void cooldownTick(Level level, BlockPos pos, BlockState state,
                                    ZirconCampfireBlockEntity be) {
        boolean changed = false;
        for (int i = 0; i < be.items.size(); i++) {
            if (be.cookingProgress[i] > 0) {
                changed = true;
                be.cookingProgress[i] = Mth.clamp(
                        be.cookingProgress[i] - BURN_COOL_SPEED,
                        0, be.cookingTime[i]);
            }
        }
        if (changed) setChanged(level, pos, state);
    }

    // -----------------------------------------------------------------------
    // Particle tick
    // -----------------------------------------------------------------------

    public static void particleTick(Level level, BlockPos pos, BlockState state,
                                    ZirconCampfireBlockEntity be) {
        RandomSource random = level.random;

        // Ванильный дым
        if (random.nextFloat() < 0.11f) {
            for (int i = 0; i < random.nextInt(2) + 2; i++) {
                CampfireBlock.makeParticles(level, pos,
                        state.getValue(CampfireBlock.SIGNAL_FIRE), false);
            }
        }

        // Голубое пламя циркона
        if (random.nextInt(4) == 0) {
            level.addParticle(ModParticles.ZIRCON_FLAME.get(),
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    pos.getY() + 0.3 + random.nextDouble() * 0.2,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4,
                    0, 0.02, 0);
        }

        // Soul частицы
        if (random.nextInt(8) == 0) {
            level.addParticle(ModParticles.ZIRCON_PARTICLES.get(),
                    pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    pos.getY() + 0.4,
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.01, 0);
        }

        // Дым от готовящейся еды
        int facing = state.getValue(CampfireBlock.FACING).get2DDataValue();
        for (int j = 0; j < be.items.size(); j++) {
            if (!be.items.get(j).isEmpty() && random.nextFloat() < 0.2f) {
                Direction dir = Direction.from2DDataValue(Math.floorMod(j + facing, 4));
                float f = 0.3125f;
                double x = pos.getX() + 0.5 - dir.getStepX() * f
                        + dir.getClockWise().getStepX() * f;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5 - dir.getStepZ() * f
                        + dir.getClockWise().getStepZ() * f;
                for (int k = 0; k < 4; k++) {
                    level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 5.0E-4, 0);
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // placeFood
    // -----------------------------------------------------------------------

    public boolean placeFood(@Nullable LivingEntity entity,
                             ItemStack food, int cookTime) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty()) {
                this.cookingTime[i] = Math.max(1,
                        Math.round(cookTime / COOK_SPEED_MULTIPLIER));
                this.cookingProgress[i] = 0;
                this.items.set(i, food.consumeAndReturn(1, entity));
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(),
                        GameEvent.Context.of(entity, this.getBlockState()));
                this.markUpdated();
                return true;
            }
        }
        return false;
    }

    public NonNullList<ItemStack> getItems() { return items; }

    public Optional<RecipeHolder<CampfireCookingRecipe>> getCookableRecipe(ItemStack stack) {
        return this.items.stream().noneMatch(ItemStack::isEmpty)
                ? Optional.empty()
                : this.quickCheck.getRecipeFor(new SingleRecipeInput(stack), this.level);
    }

    private void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(),
                this.getBlockState(), this.getBlockState(), 3);
    }

    public void dowse() {
        if (this.level != null) this.markUpdated();
    }

    @Override
    public void clearContent() { this.items.clear(); }

    // -----------------------------------------------------------------------
    // NBT
    // -----------------------------------------------------------------------

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items.clear();
        ContainerHelper.loadAllItems(tag, this.items, registries);
        if (tag.contains("CookingTimes", 11)) {
            int[] times = tag.getIntArray("CookingTimes");
            System.arraycopy(times, 0, this.cookingProgress, 0,
                    Math.min(this.cookingTime.length, times.length));
        }
        if (tag.contains("CookingTotalTimes", 11)) {
            int[] totalTimes = tag.getIntArray("CookingTotalTimes");
            System.arraycopy(totalTimes, 0, this.cookingTime, 0,
                    Math.min(this.cookingTime.length, totalTimes.length));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        tag.putIntArray("CookingTimes", this.cookingProgress);
        tag.putIntArray("CookingTotalTimes", this.cookingTime);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, this.items, true, registries);
        return tag;
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput input) {
        super.applyImplicitComponents(input);
        input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
                .copyInto(this.getItems());
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CONTAINER,
                ItemContainerContents.fromItems(this.getItems()));
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("Items");
    }
}