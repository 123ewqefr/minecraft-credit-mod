package com.creditmod.registry;

import com.creditmod.CreditMod;
import com.creditmod.gui.CreditChestContainer;
import com.creditmod.gui.CreditShopContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModContainers {

    public static final DeferredRegister<ContainerType<?>> CONTAINERS =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, CreditMod.MOD_ID);

    public static final RegistryObject<ContainerType<CreditShopContainer>> CREDIT_SHOP =
            CONTAINERS.register("credit_shop",
                    () -> IForgeContainerType.create((windowId, inv, data) -> {
                        int entityId = data.readInt();
                        return new CreditShopContainer(windowId, inv, entityId);
                    }));

    public static final RegistryObject<ContainerType<CreditChestContainer>> CREDIT_CHEST =
            CONTAINERS.register("credit_chest",
                    () -> IForgeContainerType.create((windowId, inv, data) ->
                            new CreditChestContainer(windowId, inv)));
}
