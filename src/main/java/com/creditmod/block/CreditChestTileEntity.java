package com.creditmod.block;

import com.creditmod.gui.CreditChestContainer;
import com.creditmod.registry.ModTileEntities;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class CreditChestTileEntity extends LockableLootTileEntity {

    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    // UUID владельца (кто установил сундук)
    private String ownerUUID = "";

    public CreditChestTileEntity() {
        super(ModTileEntities.CREDIT_CHEST.get());
    }

    public void setOwnerUUID(String uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public int getContainerSize() {
        return 27;
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
    protected ITextComponent getDefaultName() {
        return new StringTextComponent("§6§l💳 Кредитный сундук");
    }

    @Override
    protected Container createMenu(int id, PlayerInventory playerInventory) {
        return new CreditChestContainer(id, playerInventory, this);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        super.save(nbt);
        if (!trySaveLootTable(nbt)) {
            ItemStackHelper.saveAllItems(nbt, items);
        }
        nbt.putString("OwnerUUID", ownerUUID);
        return nbt;
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!tryLoadLootTable(nbt)) {
            ItemStackHelper.loadAllItems(nbt, items);
        }
        ownerUUID = nbt.getString("OwnerUUID");
    }

    public int countDiamonds() {
        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem() == net.minecraft.item.Items.DIAMOND) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public void clearAllDiamonds() {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty() && stack.getItem() == net.minecraft.item.Items.DIAMOND) {
                items.set(i, ItemStack.EMPTY);
            }
        }
        setChanged();
    }

    public void dropContents(World world, BlockPos pos) {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                net.minecraft.entity.item.ItemEntity drop = new net.minecraft.entity.item.ItemEntity(
                        world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
                world.addFreshEntity(drop);
            }
        }
        items = NonNullList.withSize(27, ItemStack.EMPTY);
    }
}
