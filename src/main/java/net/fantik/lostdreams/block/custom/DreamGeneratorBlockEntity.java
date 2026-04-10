package net.fantik.lostdreams.block.custom;

import net.fantik.lostdreams.block.custom.DreamGeneratorBlock;
import net.fantik.lostdreams.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class DreamGeneratorBlockEntity extends BaseContainerBlockEntity {
    public static final int SLOT_COUNT = 3;
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final Random random = new Random();

    public DreamGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DREAM_GENERATOR.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.lostdreams.dream_generator");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new DreamGeneratorMenu(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true; // Можно положить любые предметы
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    public void generateRandomResource() {
        if (level == null || level.isClientSide) return;

        boolean generated = false;
        ItemStack resource = ItemStack.EMPTY;

        // 50% шанс на уголь, 50% на железо
        if (random.nextBoolean()) {
            int amount = random.nextInt(3) + 1; // 1-3
            resource = new ItemStack(Items.COAL, amount);
        } else {
            int amount = random.nextInt(3) + 1; // 1-3
            resource = new ItemStack(Items.RAW_IRON, amount);
        }

        // Пытаемся добавить в слоты
        for (int i = 0; i < items.size(); i++) {
            ItemStack slotStack = items.get(i);
            if (slotStack.isEmpty()) {
                items.set(i, resource);
                generated = true;
                break;
            } else if (ItemStack.isSameItem(slotStack, resource) &&
                    slotStack.getCount() + resource.getCount() <= slotStack.getMaxStackSize()) {
                slotStack.grow(resource.getCount());
                generated = true;
                break;
            }
        }

        if (generated) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);

            // Обновляем состояние блока
            level.setBlock(worldPosition, getBlockState().setValue(DreamGeneratorBlock.HAS_RESOURCE, true),
                    Block.UPDATE_ALL);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DreamGeneratorBlockEntity be) {
        // Проверяем, пуст ли инвентарь для обновления состояния блока
        boolean hasItems = false;
        for (ItemStack stack : be.items) {
            if (!stack.isEmpty()) {
                hasItems = true;
                break;
            }
        }

        if (state.getValue(DreamGeneratorBlock.HAS_RESOURCE) != hasItems) {
            level.setBlock(pos, state.setValue(DreamGeneratorBlock.HAS_RESOURCE, hasItems), Block.UPDATE_ALL);
        }
    }

    // Синхронизация с клиентом
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
    }
}