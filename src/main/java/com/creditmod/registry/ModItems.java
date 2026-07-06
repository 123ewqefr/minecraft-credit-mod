package com.creditmod.registry;

import com.creditmod.CreditMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreditMod.MOD_ID);

    public static final RegistryObject<Item> CREDIT_CHEST_ITEM =
            ITEMS.register("credit_chest_block",
                    () -> new BlockItem(ModBlocks.CREDIT_CHEST_BLOCK.get(),
                            new Item.Properties().tab(ItemGroup.TAB_MISC)));

    private static <T extends Block> RegistryObject<Item> fromBlock(RegistryObject<T> block) {
        return ITEMS.register(block.getId().getPath(),
                () -> new BlockItem(block.get(), new Item.Properties().tab(ItemGroup.TAB_MISC)));
    }
}
