package com.creditmod.gui;

import com.creditmod.data.CreditShopItems;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class CreditShopScreen extends ContainerScreen<CreditShopContainer> {

    // Размер экрана
    private static final int W = 320;
    private static final int H = 220;
    // Параметры сетки товаров
    private static final int COLS = 9;
    private static final int ROWS = 5;
    private static final int SLOT_SIZE = 20;
    private static final int GRID_X_OFF = 8;
    private static final int GRID_Y_OFF = 30;

    private int currentPage = 0;
    private final int[] itemCounts;   // сколько раз кликнули по каждому товару
    private int selectedIndex = -1;   // ЛКМ — выбрать товар; ПКМ — подтвердить
    private String statusMessage = "§eНажмите ЛКМ на товар — выбрать/увеличить кол-во. ПКМ — подтвердить и оформить кредит.";

    private final List<CreditShopItems.ShopEntry> allItems;
    private int totalPages;

    public CreditShopScreen(CreditShopContainer container, PlayerInventory playerInv, ITextComponent title) {
        super(container, playerInv, title);
        this.imageWidth = W;
        this.imageHeight = H;
        allItems = CreditShopItems.getAllItems();
        totalPages = (int) Math.ceil(allItems.size() / (double) (COLS * ROWS));
        itemCounts = new int[allItems.size()];
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(MatrixStack ms, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.leftPos;
        int y = this.topPos;

        // Тёмный фон
        AbstractGui.fill(ms, x, y, x + W, y + H, 0xCC111111);
        AbstractGui.fill(ms, x + 2, y + 2, x + W - 2, y + H - 2, 0xCC1A1A2E);

        // Заголовок
        AbstractGui.fill(ms, x, y, x + W, y + 22, 0xCC0D0D0D);

        // Разделитель нижнего статуса
        AbstractGui.fill(ms, x, y + H - 36, x + W, y + H - 35, 0xFF555555);

        // Рамки ячеек товаров
        int startIdx = currentPage * COLS * ROWS;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = startIdx + row * COLS + col;
                int sx = x + GRID_X_OFF + col * SLOT_SIZE;
                int sy = y + GRID_Y_OFF + row * SLOT_SIZE;
                if (idx < allItems.size()) {
                    int bg = idx == selectedIndex ? 0xCC2255AA : 0xCC2A2A3A;
                    AbstractGui.fill(ms, sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, bg);
                    AbstractGui.fill(ms, sx, sy, sx + SLOT_SIZE, sy + 1, 0xFF444466);
                    AbstractGui.fill(ms, sx, sy, sx + 1, sy + SLOT_SIZE, 0xFF444466);
                }
            }
        }
    }

    @Override
    public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

        int x = this.leftPos;
        int y = this.topPos;

        // Заголовок
        this.font.draw(ms, "§6§l✦ Кредитное агентство ✦", x + 8, y + 6, 0xFFD700);
        this.font.draw(ms, "Стр. " + (currentPage + 1) + "/" + totalPages, x + W - 50, y + 6, 0xAAAAAA);

        // Товары
        int startIdx = currentPage * COLS * ROWS;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = startIdx + row * COLS + col;
                if (idx >= allItems.size()) break;
                CreditShopItems.ShopEntry entry = allItems.get(idx);
                int sx = x + GRID_X_OFF + col * SLOT_SIZE;
                int sy = y + GRID_Y_OFF + row * SLOT_SIZE;
                ItemStack stack = entry.stack.copy();
                this.itemRenderer.renderGuiItem(stack, sx + 1, sy + 1);
                // Показать количество если > 0
                if (itemCounts[idx] > 0) {
                    this.font.drawShadow(ms, String.valueOf(itemCounts[idx]), sx + 12, sy + 11, 0xFFFFFF);
                }
            }
        }

        // Тултип при наведении
        int startIdx2 = currentPage * COLS * ROWS;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = startIdx2 + row * COLS + col;
                if (idx >= allItems.size()) break;
                int sx = x + GRID_X_OFF + col * SLOT_SIZE;
                int sy = y + GRID_Y_OFF + row * SLOT_SIZE;
                if (mouseX >= sx && mouseX < sx + SLOT_SIZE && mouseY >= sy && mouseY < sy + SLOT_SIZE) {
                    CreditShopItems.ShopEntry entry = allItems.get(idx);
                    List<ITextComponent> tooltip = new ArrayList<>();
                    tooltip.add(entry.stack.getHoverName());
                    tooltip.add(new StringTextComponent("§7Стоимость: §6" + entry.diamondCost + " алмаз(а)"));
                    tooltip.add(new StringTextComponent("§7Выбрано: §f" + itemCounts[idx] + " шт."));
                    tooltip.add(new StringTextComponent("§eЛКМ §7— добавить | §ePКМ §7— подтвердить выбор"));
                    this.renderComponentTooltip(ms, tooltip, mouseX, mouseY);
                }
            }
        }

        // Статус-строка
        this.font.draw(ms, statusMessage, x + 8, y + H - 30, 0xDDDDDD);

        // Итог (сумма кредита)
        int total = calcTotalCost();
        if (total > 0) {
            this.font.draw(ms, "§aИтого: §6" + total + " §7алм.", x + 8, y + H - 20, 0xFFFFFF);
        }

        // Кнопки навигации / отмена
        drawNavButtons(ms, x, y);
    }

    private void drawNavButtons(MatrixStack ms, int x, int y) {
        // Кнопка «◀ Назад»
        AbstractGui.fill(ms, x + W - 120, y + H - 18, x + W - 80, y + H - 4, 0xCC334455);
        this.font.draw(ms, "◀ Назад", x + W - 118, y + H - 15, 0xAAFFFF);
        // Кнопка «Вперёд ▶»
        AbstractGui.fill(ms, x + W - 76, y + H - 18, x + W - 36, y + H - 4, 0xCC334455);
        this.font.draw(ms, "Вперёд ▶", x + W - 74, y + H - 15, 0xAAFFFF);
        // Кнопка «✕ Отмена»
        AbstractGui.fill(ms, x + W - 32, y + H - 18, x + W - 2, y + H - 4, 0xCC882222);
        this.font.draw(ms, "✕ Отмена", x + W - 30, y + H - 15, 0xFF6666);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = this.leftPos;
        int y = this.topPos;
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Кнопка «Назад»
        if (mx >= x + W - 120 && mx < x + W - 80 && my >= y + H - 18 && my < y + H - 4) {
            if (currentPage > 0) currentPage--;
            return true;
        }
        // Кнопка «Вперёд»
        if (mx >= x + W - 76 && mx < x + W - 36 && my >= y + H - 18 && my < y + H - 4) {
            if (currentPage < totalPages - 1) currentPage++;
            return true;
        }
        // Кнопка «Отмена»
        if (mx >= x + W - 32 && mx < x + W - 2 && my >= y + H - 18 && my < y + H - 4) {
            // Отправить команду отмены на сервер
            Minecraft.getInstance().player.chat("/credit cancel");
            this.onClose();
            return true;
        }

        // Клики по товарам
        int startIdx = currentPage * COLS * ROWS;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int idx = startIdx + row * COLS + col;
                if (idx >= allItems.size()) break;
                int sx = x + GRID_X_OFF + col * SLOT_SIZE;
                int sy = y + GRID_Y_OFF + row * SLOT_SIZE;
                if (mx >= sx && mx < sx + SLOT_SIZE && my >= sy && my < sy + SLOT_SIZE) {
                    if (button == 0) {
                        // ЛКМ — увеличить кол-во
                        itemCounts[idx]++;
                        selectedIndex = idx;
                        statusMessage = "§f" + allItems.get(idx).stack.getHoverName().getString()
                                + "§7: выбрано §a" + itemCounts[idx] + " шт.§7  ПКМ — подтвердить";
                    } else if (button == 1) {
                        // ПКМ — подтвердить выбор этого товара
                        if (itemCounts[idx] > 0) {
                            selectedIndex = idx;
                            confirmSelection(idx);
                        } else {
                            statusMessage = "§cСначала выберите количество (ЛКМ)!";
                        }
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void confirmSelection(int idx) {
        CreditShopItems.ShopEntry entry = allItems.get(idx);
        int qty = itemCounts[idx];
        int cost = entry.diamondCost * qty;
        // Отправить команду оформления кредита на сервер
        Minecraft.getInstance().player.chat(
                "/credit take " + entry.stack.getItem().getRegistryName().toString() + " " + qty + " " + cost);
        statusMessage = "§aОтличный выбор! Оформляем кредит на §6" + qty + " x " + entry.stack.getHoverName().getString()
                + " §a= §6" + cost + " §aалм.";
    }

    private int calcTotalCost() {
        int total = 0;
        for (int i = 0; i < allItems.size() && i < itemCounts.length; i++) {
            total += allItems.get(i).diamondCost * itemCounts[i];
        }
        return total;
    }

    @Override
    protected void renderLabels(MatrixStack ms, int mouseX, int mouseY) {
        // Ничего не рисуем (убираем стандартные метки инвентаря)
    }
}
