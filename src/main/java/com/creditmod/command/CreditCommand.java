package com.creditmod.command;

import com.creditmod.data.CreditWorldData;
import com.creditmod.data.PlayerCreditInfo;
import com.creditmod.entity.CreditAgencyEntity;
import com.creditmod.registry.ModEntities;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class CreditCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("credit")
                        .then(Commands.literal("history")
                                .executes(ctx -> showHistory(ctx.getSource())))
                        .then(Commands.literal("cancel")
                                .executes(ctx -> cancelCredit(ctx.getSource())))
                        .then(Commands.literal("take")
                                .then(Commands.argument("item", StringArgumentType.string())
                                        .then(Commands.argument("qty", IntegerArgumentType.integer(1, 64))
                                                .then(Commands.argument("cost", IntegerArgumentType.integer(1, 9999))
                                                        .executes(ctx -> takeCredit(ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "item"),
                                                                IntegerArgumentType.getInteger(ctx, "qty"),
                                                                IntegerArgumentType.getInteger(ctx, "cost")))))))
                        .then(Commands.literal("pay")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 9999))
                                        .executes(ctx -> payCredit(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")))))
                        .executes(ctx -> spawnAgency(ctx.getSource()))
        );
    }

    /** /credit — Вызвать НПС Кредитного агентства */
    private static int spawnAgency(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) return 0;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        ServerWorld world = player.getLevel();

        CreditAgencyEntity agency = ModEntities.CREDIT_AGENCY.get().create(world);
        if (agency == null) return 0;

        // Спавн перед игроком
        double angle = Math.toRadians(player.yRot);
        double spawnX = player.getX() - Math.sin(angle) * 3;
        double spawnZ = player.getZ() + Math.cos(angle) * 3;
        agency.moveTo(spawnX, player.getY(), spawnZ, 0, 0);
        world.addFreshEntity(agency);

        // Сообщение в чат
        agency.sendNpcMessage(player,
                "§6§l✦ Кредитное агентство: §r§eДобро пожаловать в наше кредитное агентство! " +
                        "§7Нажмите на меня §e§lПКМ§r§7, чтобы открыть меню кредитования.");

        return 1;
    }

    /** /credit history — Показать кредитную историю */
    private static int showHistory(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) return 0;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        CreditWorldData data = CreditWorldData.get(player.getLevel());
        PlayerCreditInfo info = data.getOrCreate(player.getUUID());

        player.sendMessage(new StringTextComponent(""), player.getUUID());
        player.sendMessage(new StringTextComponent("§6§l╔══ 📋 КРЕДИТНАЯ ИСТОРИЯ ══╗"), player.getUUID());
        player.sendMessage(new StringTextComponent("§7Игрок: §f" + player.getName().getString()), player.getUUID());
        player.sendMessage(new StringTextComponent("§7Кредитный рейтинг: " + info.getCreditScoreLabel()), player.getUUID());
        player.sendMessage(new StringTextComponent("§7Базовая ставка: §c" + (int)(info.getInterestRatePerMinute() * 100) + "%§7/мин"), player.getUUID());

        if (info.hasPendingCredit()) {
            player.sendMessage(new StringTextComponent("§6§l--- Активный кредит ---"), player.getUUID());
            player.sendMessage(new StringTextComponent("§7Изначальная сумма: §a" + String.format("%.2f", info.getInitialCredit()) + " §7алм."), player.getUUID());
            player.sendMessage(new StringTextComponent("§7Текущий долг: §c" + String.format("%.2f", info.getCurrentDebt()) + " §7алм. (с процентами)"), player.getUUID());
            int daysWithout = info.getDaysWithoutPayment();
            player.sendMessage(new StringTextComponent("§7Дней без платежа: §e" + daysWithout + "§7 из §c" + info.getRepaymentDays()), player.getUUID());
            if (info.getCreditItemsDescription() != null && !info.getCreditItemsDescription().isEmpty()) {
                player.sendMessage(new StringTextComponent("§7Товары: §f" + info.getCreditItemsDescription()), player.getUUID());
            }
        } else {
            player.sendMessage(new StringTextComponent("§a✓ Активных кредитов нет"), player.getUUID());
        }

        player.sendMessage(new StringTextComponent("§7Платежей по истории: §f" + info.getPaymentsThisCredit()), player.getUUID());
        player.sendMessage(new StringTextComponent("§6§l╚════════════════════════╝"), player.getUUID());
        return 1;
    }

    /** /credit cancel — Отмена оформления */
    private static int cancelCredit(CommandSource source) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) return 0;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        player.sendMessage(new StringTextComponent(
                "§c§l[Кредитное агентство]: §r§7Вы отменили оформление кредита. " +
                        "Возвращайтесь, если понадоблюсь!"), player.getUUID());
        // Убрать НПС поблизости через корректный 1.16.5 API
        net.minecraft.util.math.AxisAlignedBB area = player.getBoundingBox().inflate(20);
        player.getLevel()
                .getEntitiesOfClass(CreditAgencyEntity.class, area, e -> true)
                .forEach(e -> e.remove());
        return 1;
    }

    /**
     * /credit take <item> <qty> <cost> — Оформить кредит (вызывается из GUI)
     */
    private static int takeCredit(CommandSource source, String itemName, int qty, int totalCost) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) return 0;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        ServerWorld world = player.getLevel();

        CreditWorldData creditData = CreditWorldData.get(world);
        PlayerCreditInfo info = creditData.getOrCreate(player.getUUID());

        if (info.getCreditScore() > 400) {
            player.sendMessage(new StringTextComponent(
                    "§4§l[Кредитное агентство]: §r§cВам отказано в кредите. " +
                            "Ваш рейтинг (§4" + info.getCreditScore() + "§c) превышает допустимый порог 400. " +
                            "Сначала погасите текущие обязательства."), player.getUUID());
            return 0;
        }

        if (info.hasPendingCredit()) {
            player.sendMessage(new StringTextComponent(
                    "§4§l[Кредитное агентство]: §r§cУ вас уже есть активный кредит! " +
                            "Задолженность: §e" + String.format("%.2f", info.getCurrentDebt()) + " §cалм."), player.getUUID());
            return 0;
        }

        // Записать кредит
        info.setInitialCredit(totalCost);
        info.setCurrentDebt(totalCost);
        info.setHasPendingCredit(true);
        info.setCreditTakenTick(world.getGameTime());
        info.setLastPaymentTick(world.getGameTime());
        info.setLastInterestTick(world.getGameTime());
        info.setCreditItemsDescription(itemName + " x" + qty);

        // Выдать товары (ищем по имени)
        net.minecraft.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.util.ResourceLocation(itemName));
        if (item != null && item != Items.AIR) {
            ItemStack creditItem = new ItemStack(item, qty);
            player.inventory.add(creditItem);
        }

        // Выдать кредитный договор (зачарованная книга с условиями)
        player.inventory.add(createCreditContract(info));

        // Выдать кредитный сундук (для погашения)
        ItemStack chestItem = createCreditChestItem(info);
        player.inventory.add(chestItem);

        // Выдать «Отказ» (барьер)
        ItemStack refusal = new ItemStack(Items.BARRIER);
        CompoundNBT refTag = new CompoundNBT();
        CompoundNBT display = new CompoundNBT();
        display.putString("Name", "{\"text\":\"§c§lОтказ от кредита\",\"italic\":false}");
        ListNBT loreRefusal = new ListNBT();
        loreRefusal.add(StringNBT.valueOf("{\"text\":\"§7Кликните по НПС, чтобы\",\"italic\":false}"));
        loreRefusal.add(StringNBT.valueOf("{\"text\":\"§7отказаться от кредита.\",\"italic\":false}"));
        display.put("Lore", loreRefusal);
        refTag.put("display", display);
        refTag.putBoolean("CreditRefusal", true);
        refusal.setTag(refTag);
        player.inventory.add(refusal);

        creditData.markChanged();

        // Сообщения в чат
        player.sendMessage(new StringTextComponent(
                "§6§l[Кредитное агентство]: §r§aОтлично! Осталось совсем немного. " +
                        "§eКликните договором по мне, чтобы оформить кредит."), player.getUUID());

        // Закрыть интерфейс магазина
        player.closeContainer();

        return 1;
    }

    /**
     * /credit pay <amount> — Внести платёж (вызывается из обработчика сундука)
     */
    public static int payCredit(CommandSource source, int diamonds) {
        if (!(source.getEntity() instanceof ServerPlayerEntity)) return 0;
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        return processCreditPayment(player, diamonds);
    }

    public static int processCreditPayment(ServerPlayerEntity player, int diamonds) {
        ServerWorld world = player.getLevel();
        CreditWorldData creditData = CreditWorldData.get(world);
        PlayerCreditInfo info = creditData.getOrCreate(player.getUUID());

        if (!info.hasPendingCredit()) {
            player.sendMessage(new StringTextComponent(
                    "§7[Кредитное агентство]: У вас нет активных кредитов."), player.getUUID());
            return 0;
        }

        double oldDebt = info.getCurrentDebt();
        double newDebt = Math.max(0, oldDebt - diamonds);
        info.setCurrentDebt(newDebt);
        info.setLastPaymentTick(world.getGameTime());
        info.setDaysWithoutPayment(0);
        info.incrementPayments();

        player.sendMessage(new StringTextComponent(
                "§6§l[Кредитное агентство]: §r§aВнесено §6" + diamonds +
                        " §aалм. │ Остаток долга: §c" + String.format("%.2f", newDebt) + " §aалм."), player.getUUID());

        if (newDebt <= 0) {
            // Кредит погашен!
            closeCreditSuccess(player, info, world);
        }

        creditData.markChanged();
        return 1;
    }

    private static void closeCreditSuccess(ServerPlayerEntity player, PlayerCreditInfo info, ServerWorld world) {
        long takenTick = info.getCreditTakenTick();
        long nowTick = world.getGameTime();
        long ticksTaken = nowTick - takenTick;
        long mcDayTicks = 24000L;

        int scoreChange;
        if (ticksTaken <= mcDayTicks) {
            scoreChange = -30; // Закрыл в тот же день
        } else if (ticksTaken <= 2 * mcDayTicks) {
            scoreChange = -5;  // Каждые ~1 день
        } else if (ticksTaken <= 4 * mcDayTicks) {
            scoreChange = -2;  // Каждые 2 дня
        } else {
            scoreChange = -1;  // 3-4 дня
        }

        info.addCreditScore(scoreChange);
        info.setHasPendingCredit(false);
        info.setCurrentDebt(0);
        info.setInitialCredit(0);
        info.setDaysWithoutPayment(0);
        info.setCreditChestPos(Long.MIN_VALUE);
        info.setCreditItemsDescription("");

        player.sendMessage(new StringTextComponent(""), player.getUUID());
        player.sendMessage(new StringTextComponent(
                "§a§l╔════════════════════════════╗"), player.getUUID());
        player.sendMessage(new StringTextComponent(
                "§a§l║  🎉 КРЕДИТ ПОЛНОСТЬЮ ПОГАШЕН!  ║"), player.getUUID());
        player.sendMessage(new StringTextComponent(
                "§a§l╚════════════════════════════╝"), player.getUUID());
        player.sendMessage(new StringTextComponent(
                "§7Изменение рейтинга: §a" + scoreChange +
                        " │ Новый рейтинг: " + info.getCreditScoreLabel()), player.getUUID());
        player.sendMessage(new StringTextComponent(""), player.getUUID());
    }

    // === Утилиты ===

    public static ItemStack createCreditContract(PlayerCreditInfo info) {
        ItemStack contract = new ItemStack(Items.WRITTEN_BOOK);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putString("title", "Кредитный договор");
        nbt.putString("author", "Кредитное агентство");
        nbt.putByte("resolved", (byte) 1);

        ListNBT pages = new ListNBT();
        String page1 = "{\"text\":\"§6§l✦ КРЕДИТНЫЙ ДОГОВОР ✦\\n\\n" +
                "§0Настоящий договор заключён между\\n" +
                "Кредитным агентством\\n" +
                "и заёмщиком.\\n\\n" +
                "§4§lСУММА КРЕДИТА:\\n" +
                "§0" + String.format("%.0f", info.getInitialCredit()) + " алмаз(а)\\n\\n" +
                "§c§lПРОЦЕНТНАЯ СТАВКА:\\n" +
                "§0" + (int)(info.getInterestRatePerMinute() * 100) + "% в минуту\\n" +
                "от суммы долга\"}";
        pages.add(StringNBT.valueOf(page1));

        String page2 = "{\"text\":\"§4§lУСЛОВИЯ ПОГАШЕНИЯ:\\n\\n" +
                "§0Срок: §c" + info.getRepaymentDays() + " §0игровых дней\\n\\n" +
                "Положите алмазы в\\n" +
                "Кредитный сундук.\\n\\n" +
                "§4§lПРЕДУПРЕЖДЕНИЕ:\\n" +
                "§0При просрочке 5 дней —\\n" +
                "явятся коллекторы!\\n" +
                "При просрочке 10 дней —\\n" +
                "изымается весь инвентарь!\"}";
        pages.add(StringNBT.valueOf(page2));

        String page3 = "{\"text\":\"§6§lКРЕДИТНАЯ ИСТОРИЯ:\\n\\n" +
                "§0Ваш текущий рейтинг:\\n" + info.getCreditScoreLabel().replace("§", "\\u00a7") + "\\n\\n" +
                "Хорошая история\\n" +
                "снижает ставку!\\n\\n" +
                "§4Нажмите договором\\n" +
                "§4по НПС для подписания.\\n\\n" +
                "§6§l— Кредитное агентство —\"}";
        pages.add(StringNBT.valueOf(page3));

        nbt.put("pages", pages);

        // Внешний вид
        CompoundNBT display = new CompoundNBT();
        display.putString("Name", "{\"text\":\"§6§l📜 Кредитный договор\",\"italic\":false}");
        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"§7Прочитайте условия и кликните\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"§7договором по НПС для подписания.\",\"italic\":false}"));
        display.put("Lore", lore);
        nbt.put("display", display);
        nbt.putBoolean("IsCreditContract", true);

        contract.setTag(nbt);
        return contract;
    }

    public static ItemStack createCreditChestItem(PlayerCreditInfo info) {
        ItemStack chestItem = new ItemStack(
                com.creditmod.registry.ModItems.CREDIT_CHEST_ITEM.get());
        CompoundNBT nbt = chestItem.getOrCreateTag();
        CompoundNBT display = new CompoundNBT();
        display.putString("Name", "{\"text\":\"§6§l💳 Кредитный сундук\",\"italic\":false}");
        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"§7Поставьте на землю и\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"§7положите алмазы для погашения.\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"§cДолг: §f" + String.format("%.2f", info.getCurrentDebt()) + " алм.\",\"italic\":false}"));
        display.put("Lore", lore);
        nbt.put("display", display);
        nbt.putBoolean("IsCreditChest", true);
        chestItem.setTag(nbt);
        return chestItem;
    }
}
