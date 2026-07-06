package com.creditmod.entity.render;

import com.creditmod.entity.CollectorEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.util.ResourceLocation;

public class CollectorRenderer extends MobRenderer<CollectorEntity, ZombieModel<CollectorEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("minecraft", "textures/entity/zombie/zombie.png");

    public CollectorRenderer(EntityRendererManager manager) {
        super(manager, new ZombieModel<>(0, false), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(CollectorEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(CollectorEntity entity, MatrixStack stack, float ticks) {
        // Коллекторы немного крупнее обычных зомби
        stack.scale(1.1F, 1.1F, 1.1F);
    }
}
