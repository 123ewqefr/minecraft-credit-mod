package com.creditmod.data;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Список всех предметов, доступных в кредит.
 */
public class CreditShopItems {

    public static class ShopEntry {
        public final ItemStack stack;
        public final int diamondCost; // Цена в алмазах за 1 штуку

        public ShopEntry(ItemStack stack, int diamondCost) {
            this.stack = stack;
            this.diamondCost = diamondCost;
        }
    }

    private static final List<ShopEntry> ALL_ITEMS = new ArrayList<>();

    static {
        // ===== РУДЫ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.COAL_ORE), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_ORE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLD_ORE), 3));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_ORE), 5));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.EMERALD_ORE), 5));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.LAPIS_ORE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.REDSTONE_ORE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHER_QUARTZ_ORE), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHER_GOLD_ORE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.ANCIENT_DEBRIS), 10));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GILDED_BLACKSTONE), 3));

        // ===== МЕЧИ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.WOODEN_SWORD), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.STONE_SWORD), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_SWORD), 4));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_SWORD), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_SWORD), 10));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_SWORD), 25));

        // ===== КИРКИ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.WOODEN_PICKAXE), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.STONE_PICKAXE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_PICKAXE), 4));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_PICKAXE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_PICKAXE), 10));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_PICKAXE), 25));

        // ===== МОТЫГИ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.WOODEN_HOE), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.STONE_HOE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_HOE), 3));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_HOE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_HOE), 8));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_HOE), 16));

        // ===== ЛОПАТЫ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.WOODEN_SHOVEL), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.STONE_SHOVEL), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_SHOVEL), 3));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_SHOVEL), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_SHOVEL), 8));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_SHOVEL), 16));

        // ===== ТОПОРЫ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.WOODEN_AXE), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.STONE_AXE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_AXE), 4));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_AXE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_AXE), 10));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_AXE), 25));

        // ===== БРОНЯ — КОЖА =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.LEATHER_HELMET), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.LEATHER_CHESTPLATE), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.LEATHER_LEGGINGS), 1));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.LEATHER_BOOTS), 1));

        // ===== БРОНЯ — КОЛЬЧУГА =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.CHAINMAIL_HELMET), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 3));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.CHAINMAIL_LEGGINGS), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.CHAINMAIL_BOOTS), 2));

        // ===== БРОНЯ — ЖЕЛЕЗО =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_HELMET), 3));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_CHESTPLATE), 5));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_LEGGINGS), 4));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.IRON_BOOTS), 3));

        // ===== БРОНЯ — ЗОЛОТО =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_HELMET), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_CHESTPLATE), 3));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_LEGGINGS), 2));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.GOLDEN_BOOTS), 2));

        // ===== БРОНЯ — АЛМАЗ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_HELMET), 10));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_CHESTPLATE), 15));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_LEGGINGS), 12));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.DIAMOND_BOOTS), 8));

        // ===== БРОНЯ — НЕЗЕРИТ =====
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_HELMET), 20));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_CHESTPLATE), 30));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_LEGGINGS), 25));
        ALL_ITEMS.add(new ShopEntry(new ItemStack(Items.NETHERITE_BOOTS), 15));
    }

    public static List<ShopEntry> getAllItems() {
        return ALL_ITEMS;
    }
}
