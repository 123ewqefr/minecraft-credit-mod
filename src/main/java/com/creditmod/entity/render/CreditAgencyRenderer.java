package com.creditmod.entity.render;

import com.creditmod.entity.CreditAgencyEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HeadLayer;
import net.minecraft.client.renderer.entity.model.VillagerModel;
import net.minecraft.util.ResourceLocation;

/**
 * Рендер НПС кредитного агентства — использует модель и текстуру жителя.
 * CrossedArmsItemLayer удалён: он требует AbstractVillagerEntity как тип-параметр.
 */
public class CreditAgencyRenderer extends MobRenderer<CreditAgencyEntity, VillagerModel<CreditAgencyEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/villager/villager.png");

    public CreditAgencyRenderer(EntityRendererManager manager) {
        super(manager, new VillagerModel<>(0), 0.5F);
        this.addLayer(new HeadLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CreditAgencyEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CreditAgencyEntity entity, MatrixStack stack, float ticks) {
        // Масштаб 1:1, без изменений
    }
}
