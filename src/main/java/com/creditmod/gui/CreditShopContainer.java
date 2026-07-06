package com.creditmod.gui;

import com.creditmod.registry.ModContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class CreditShopContainer extends Container {

    private final int agencyEntityId;

    public CreditShopContainer(int windowId, PlayerInventory playerInv, int entityId) {
        super(ModContainers.CREDIT_SHOP.get(), windowId);
        this.agencyEntityId = entityId;
    }

    public int getAgencyEntityId() {
        return agencyEntityId;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        return ItemStack.EMPTY;
    }
}
