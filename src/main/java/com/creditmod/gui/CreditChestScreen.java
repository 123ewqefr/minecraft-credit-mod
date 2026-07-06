package com.creditmod.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CreditChestScreen extends ContainerScreen<CreditChestContainer> {

    private static final ResourceLocation CHEST_GUI_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public CreditChestScreen(CreditChestContainer container, PlayerInventory playerInv, ITextComponent title) {
        super(container, playerInv, new StringTextComponent("§6§l💳 Кредитный сундук — вносите алмазы!"));
        this.imageHeight = 168;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bind(CHEST_GUI_TEXTURE);
        int x = this.leftPos;
        int y = this.topPos;
        this.blit(ms, x, y, 0, 0, this.imageWidth, 3 * 18 + 17);
        this.blit(ms, x, y + 3 * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);
        this.renderTooltip(ms, mouseX, mouseY);

        // Подсказка
        int x = this.leftPos;
        int y = this.topPos;
        this.font.draw(ms, "§eПоложите алмазы — долг погасится автоматически!",
                x + 5, y - 10, 0xFFFF55);
    }
}
