package com.creditmod.entity.render;

import com.creditmod.entity.CollectorEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.util.ResourceLocation;

public class CollectorRenderer extends MobRenderer<CollectorEntity, BipedModel<CollectorEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");

    public CollectorRenderer(EntityRendererManager manager) {
        super(manager, new BipedModel<>(0.0F), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CollectorEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CollectorEntity entity, MatrixStack stack, float ticks) {
        stack.scale(1.1F, 1.1F, 1.1F);
    }
}
