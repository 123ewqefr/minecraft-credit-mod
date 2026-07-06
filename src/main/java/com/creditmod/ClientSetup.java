package com.creditmod;

import com.creditmod.entity.render.CollectorRenderer;
import com.creditmod.entity.render.CreditAgencyRenderer;
import com.creditmod.gui.CreditChestScreen;
import com.creditmod.gui.CreditShopScreen;
import com.creditmod.registry.ModContainers;
import com.creditmod.registry.ModEntities;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientSetup {

    public static void init(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(
                ModEntities.CREDIT_AGENCY.get(), CreditAgencyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(
                ModEntities.COLLECTOR.get(), CollectorRenderer::new);

        ScreenManager.register(ModContainers.CREDIT_SHOP.get(), CreditShopScreen::new);
        ScreenManager.register(ModContainers.CREDIT_CHEST.get(), CreditChestScreen::new);
    }
}
