package com.creditmod.registry;

import com.creditmod.CreditMod;
import com.creditmod.block.CreditChestTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModTileEntities {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, CreditMod.MOD_ID);

    public static final RegistryObject<TileEntityType<CreditChestTileEntity>> CREDIT_CHEST =
            TILE_ENTITIES.register("credit_chest",
                    () -> TileEntityType.Builder
                            .of(CreditChestTileEntity::new, ModBlocks.CREDIT_CHEST_BLOCK.get())
                            .build(null));
}
