package com.creditmod;

import com.creditmod.event.CreditEventHandler;
import com.creditmod.registry.ModBlocks;
import com.creditmod.registry.ModContainers;
import com.creditmod.registry.ModEntities;
import com.creditmod.registry.ModItems;
import com.creditmod.registry.ModTileEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreditMod.MOD_ID)
public class CreditMod {

    public static final String MOD_ID = "creditmod";

    public CreditMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModEntities.ENTITIES.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModContainers.CONTAINERS.register(modBus);
        ModTileEntities.TILE_ENTITIES.register(modBus);

        modBus.addListener(ModEntities::onAttributeCreate);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modBus.addListener(ClientSetup::init);
        });

        MinecraftForge.EVENT_BUS.register(new CreditEventHandler());
    }
}
