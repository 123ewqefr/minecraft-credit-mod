package com.creditmod.registry;

import com.creditmod.CreditMod;
import com.creditmod.block.CreditChestBlock;
import net.minecraft.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreditMod.MOD_ID);

    public static final RegistryObject<CreditChestBlock> CREDIT_CHEST_BLOCK =
            BLOCKS.register("credit_chest_block", CreditChestBlock::new);
}
