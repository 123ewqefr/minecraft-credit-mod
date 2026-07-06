package com.creditmod.gui;

import com.creditmod.registry.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class CreditChestContainer extends Container {

    private final IInventory chestInventory;

    public CreditChestContainer(int windowId, PlayerInventory playerInv) {
        this(windowId, playerInv, new Inventory(27));
    }

    public CreditChestContainer(int windowId, PlayerInventory playerInv, IInventory chestInv) {
        super(ModContainers.CREDIT_CHEST.get(), windowId);
        this.chestInventory = chestInv;
        checkContainerSize(chestInv, 27);
        chestInv.startOpen(playerInv.player);

        // 27 слотов сундука (3 ряда)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new DiamondOnlySlot(chestInv, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        // Инвентарь игрока
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Хотбар
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public IInventory getChestInventory() {
        return chestInventory;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 27) {
                if (!this.moveItemStackTo(stack, 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 0, 27, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        chestInventory.stopOpen(player);
    }

    /**
     * Принимает только алмазы.
     */
    private static class DiamondOnlySlot extends Slot {
        public DiamondOnlySlot(IInventory inv, int index, int x, int y) {
            super(inv, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() == Items.DIAMOND;
        }
    }
}
