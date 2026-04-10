package net.fantik.lostdreams.block.custom;

import net.fantik.lostdreams.screen.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DreamGeneratorMenu extends AbstractContainerMenu {
    private final Container container;
    private static final int SLOT_COUNT = 3;

    // Конструктор для клиента (используется IMenuTypeExtension.create)
    public DreamGeneratorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT));
    }

    // Полный конструктор
    public DreamGeneratorMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.DREAM_GENERATOR_MENU.get(), containerId);
        checkContainerSize(container, SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        // Слоты генератора (3 слота в ряд)
        for (int i = 0; i < SLOT_COUNT; i++) {
            this.addSlot(new Slot(container, i, 62 + i * 18, 35));
        }

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Хотбар
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public Container getContainer() {
        return container;
    }
}