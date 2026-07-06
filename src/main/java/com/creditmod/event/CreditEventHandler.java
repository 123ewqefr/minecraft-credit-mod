package com.creditmod.event;

import com.creditmod.block.CreditChestTileEntity;
import com.creditmod.command.CreditCommand;
import com.creditmod.data.CreditWorldData;
import com.creditmod.data.PlayerCreditInfo;
import com.creditmod.entity.CreditAgencyEntity;
import com.creditmod.entity.CollectorEntity;
import com.creditmod.registry.ModEntities;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class CreditEventHandler {

    // 1 реальная минута = 1200 тиков при 20 TPS
    private static final long TICKS_PER_MINUTE = 1200L;
    // 1 игровой день = 24000 тиков
    private static final long TICKS_PER_MC_DAY = 24000L;

    // Последний тик проверки для каждого мира
    private final Map<String, Long> lastCheckTick = new HashMap<>();
    // Позиции кредитных сундуков: worldDim -> (blockPosLong -> ownerUUID)
    private final Map<String, Map<Long, String>> creditChestPositions = new HashMap<>();

    // =====================================================================
    //  КОМАНДЫ
    // =====================================================================

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CreditCommand.register(event.getDispatcher());
    }

    // =====================================================================
    //  УСТАНОВКА КРЕДИТНОГО СУНДУКА
    // =====================================================================

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            BlockPos pos = event.getPos();

            // Проверяем: это CreditChestBlock?
            if (event.getPlacedBlock().getBlock() instanceof com.creditmod.block.CreditChestBlock) {
                // Даём TE время появиться — регистрируем позицию
                String dim = event.getWorld().dimensionType().toString();
                creditChestPositions
                        .computeIfAbsent(dim, k -> new HashMap<>())
                        .put(pos.asLong(), player.getStringUUID());

                CreditWorldData data = CreditWorldData.get(player.getLevel());
                PlayerCreditInfo info = data.getOrCreate(player.getUUID());
                info.setCreditChestPos(pos.asLong());
                data.markChanged();

                // Установить ownerUUID в TE
                net.minecraft.world.World world = (net.minecraft.world.World) event.getWorld();
                if (!world.isClientSide) {
                    TileEntity te = world.getBlockEntity(pos);
                    if (te instanceof CreditChestTileEntity) {
                        ((CreditChestTileEntity) te).setOwnerUUID(player.getStringUUID());
                    }
                }

                player.sendMessage(new StringTextComponent(
                        "§6§l[Кредитное агентство]: §r§aКредитный сундук установлен! " +
                                "Положите алмазы для погашения долга."), player.getUUID());
            }
        }
    }

    // =====================================================================
    //  ВЗАИМОДЕЙСТВИЕ С НПС (договор / отказ)
    // =====================================================================

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getWorld().isClientSide) return;

        // Клик по НПС кредитного агентства
        if (event.getTarget() instanceof CreditAgencyEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            ItemStack inHand = player.getMainHandItem();
            CreditAgencyEntity agency = (CreditAgencyEntity) event.getTarget();
            CreditWorldData data = CreditWorldData.get(player.getLevel());
            PlayerCreditInfo info = data.getOrCreate(player.getUUID());

            // Если в руке договор — подписать
            if (inHand.hasTag() && inHand.getTag().getBoolean("IsCreditContract")) {
                if (!info.hasPendingCredit()) {
                    player.sendMessage(new StringTextComponent(
                            "§c[Кредитное агентство]: У вас нет кредита для подписания."), player.getUUID());
                    return;
                }
                // Финальное подтверждение кредита
                player.sendMessage(new StringTextComponent(
                        "§6§l[Кредитное агентство]: §r§aКредит успешно оформлен! 🎉"), player.getUUID());

                long dayCount = info.getRepaymentDays();
                double debt = info.getCurrentDebt();
                double rate = info.getInterestRatePerMinute() * 100;
                player.sendMessage(new StringTextComponent(""), player.getUUID());
                player.sendMessage(new StringTextComponent("§6§l╔══ УСЛОВИЯ КРЕДИТА ══╗"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7  1. Сумма кредита: §a" + String.format("%.0f", debt) + " алм."), player.getUUID());
                player.sendMessage(new StringTextComponent("§7  2. Ставка: §c" + (int) rate + "%§7/мин (сложные проценты)"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7  3. Срок погашения: §e" + dayCount + " §7игровых дней"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7  4. Рейтинг: " + info.getCreditScoreLabel()), player.getUUID());
                player.sendMessage(new StringTextComponent("§6§l╚══════════════════════╝"), player.getUUID());
                player.sendMessage(new StringTextComponent("§7Установите кредитный сундук и кладите туда алмазы!"), player.getUUID());
                player.sendMessage(new StringTextComponent(""), player.getUUID());

                // Убрать договор из руки
                inHand.shrink(1);

                // НПС уходит
                agency.remove();
                event.setCanceled(true);
                data.markChanged();
                return;
            }

            // Если в руке отказ — отмена
            if (inHand.hasTag() && inHand.getTag().getBoolean("CreditRefusal")) {
                info.setHasPendingCredit(false);
                info.setCurrentDebt(0);
                info.setInitialCredit(0);

                // Убрать все кредитные предметы из инвентаря
                removeTaggedItemsFromInventory(player);

                player.sendMessage(new StringTextComponent(
                        "§c§l[Кредитное агентство]: §r§7Вы отказались от кредита. " +
                                "Все выданные предметы изъяты."), player.getUUID());

                agency.remove();
                event.setCanceled(true);
                data.markChanged();
            }
        }

        // Клик по коллектору с отказом — то же самое убирание
        if (event.getTarget() instanceof CollectorEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            ItemStack inHand = player.getMainHandItem();

            if (inHand.hasTag() && inHand.getTag().getBoolean("CreditRefusal")) {
                // Коллекторы игнорируют барьер отказа
                player.sendMessage(new StringTextComponent(
                        "§4§l[Коллектор]: §r§cОтказаться от долга уже нельзя! Платите!"), player.getUUID());
                event.setCanceled(true);
            }
        }
    }

    // =====================================================================
    //  СЕРВЕРНЫЙ ТИК — проценты, коллекторы, платежи из сундука
    // =====================================================================

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        event.getServer().getAllLevels().forEach(world -> {
            long gameTick = world.getGameTime();
            String dimKey = world.dimension().location().toString();
            long last = lastCheckTick.getOrDefault(dimKey, 0L);

            // Проверять каждые 100 тиков (5 секунд)
            if (gameTick - last < 100) return;
            lastCheckTick.put(dimKey, gameTick);

            CreditWorldData data = CreditWorldData.get(world);
            Collection<PlayerCreditInfo> allInfos = data.getAllPlayers();
            if (allInfos.isEmpty()) return;

            for (PlayerCreditInfo info : allInfos) {
                if (!info.hasPendingCredit()) continue;

                ServerPlayerEntity player = world.getServer().getPlayerList()
                        .getPlayer(info.getPlayerId());
                if (player == null) continue; // Игрок офлайн

                // -- 1. Начисление процентов --
                long ticksSinceInterest = gameTick - info.getLastInterestTick();
                if (ticksSinceInterest >= TICKS_PER_MINUTE) {
                    long minutesPassed = ticksSinceInterest / TICKS_PER_MINUTE;
                    double debt = info.getCurrentDebt();
                    for (long m = 0; m < minutesPassed; m++) {
                        debt = debt * (1.0 + info.getInterestRatePerMinute());
                    }
                    double added = debt - info.getCurrentDebt();
                    info.setCurrentDebt(debt);
                    info.setLastInterestTick(gameTick);

                    player.sendMessage(new StringTextComponent(
                            "§c§l⚠ [Кредитное агентство]: §r§7Начислены проценты §c+"
                                    + String.format("%.2f", added) + " §7алм. │ Текущий долг: §c"
                                    + String.format("%.2f", debt) + " §7алм."), player.getUUID());

                    // Action bar
                    player.displayClientMessage(new StringTextComponent(
                            "§c§l💳 Долг: " + String.format("%.2f", debt) + " алм."), true);
                    data.markChanged();
                }

                // -- 2. Подсчёт игровых дней без платежа --
                long ticksSincePayment = gameTick - info.getLastPaymentTick();
                int daysSincePayment = (int) (ticksSincePayment / TICKS_PER_MC_DAY);
                if (daysSincePayment != info.getDaysWithoutPayment()) {
                    info.setDaysWithoutPayment(daysSincePayment);
                    data.markChanged();
                }

                int repayDays = info.getRepaymentDays();

                // -- 3. Коллекторы (после 5 дней без платежа) --
                if (daysSincePayment >= 5 && daysSincePayment < repayDays) {
                    long lastCollectorTick = info.getLastCollectorSpawnTick();
                    // Спавнить коллекторов раз в игровой день
                    if (gameTick - lastCollectorTick >= TICKS_PER_MC_DAY) {
                        spawnCollectors(world, player, info);
                        info.setLastCollectorSpawnTick(gameTick);
                        data.markChanged();
                    }
                }

                // -- 4. Полное изъятие инвентаря (10+ дней) --
                if (daysSincePayment >= repayDays) {
                    seizeInventory(player, info, world, data, "full");
                }

                // -- 5. Проверка кредитного сундука --
                checkCreditChest(world, player, info, data, gameTick, dimKey);
            }
        });
    }

    // =====================================================================
    //  ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =====================================================================

    private void spawnCollectors(ServerWorld world, ServerPlayerEntity player, PlayerCreditInfo info) {
        player.sendMessage(new StringTextComponent(""), player.getUUID());
        player.sendMessage(new StringTextComponent(
                "§4§l☠ [Коллекторское агентство]: §r§cЗа " + info.getDaysWithoutPayment() +
                        " игровых дней вы не попытались внести ни одного платежа. " +
                        "Мы вынуждены отправить к вам коллекторов."), player.getUUID());
        player.sendMessage(new StringTextComponent(""), player.getUUID());

        for (int i = 0; i < 2; i++) {
            CollectorEntity collector = ModEntities.COLLECTOR.get().create(world);
            if (collector == null) continue;

            double angle = Math.toRadians(player.yRot + (i == 0 ? 30 : -30));
            double spawnX = player.getX() - Math.sin(angle) * 4;
            double spawnZ = player.getZ() + Math.cos(angle) * 4;
            collector.moveTo(spawnX, player.getY(), spawnZ, (float) Math.toDegrees(angle), 0);
            collector.setTargetPlayerUUID(player.getUUID());
            world.addFreshEntity(collector);
        }
    }

    private void seizeInventory(ServerPlayerEntity player, PlayerCreditInfo info,
                                ServerWorld world, CreditWorldData data, String reason) {
        double debt = info.getCurrentDebt();

        // Сначала ищем коллекторов рядом — забирают вещи как платёж
        List<CollectorEntity> nearCollectors = world.getEntitiesOfClass(
                CollectorEntity.class,
                player.getBoundingBox().inflate(16),
                c -> c.getTargetPlayerUUID() != null &&
                        c.getTargetPlayerUUID().equals(player.getUUID()));

        if (!nearCollectors.isEmpty()) {
            // Проверяем инвентарь на алмазы
            int diamonds = countDiamondsInInventory(player);
            if (diamonds > 0) {
                removeDiamondsFromInventory(player, diamonds);
                player.sendMessage(new StringTextComponent(
                        "§4§l[Коллектор]: §r§cОпа! А что у нас тут в кармашке? " +
                                "Заберём §e" + diamonds + " §cалм. в качестве долга!"), player.getUUID());
                CreditCommand.processCreditPayment(player, diamonds);
                return;
            }
        }

        // Полное изъятие при 10+ днях
        if ("full".equals(reason)) {
            player.sendMessage(new StringTextComponent(""), player.getUUID());
            player.sendMessage(new StringTextComponent(
                    "§4§l☠ [Коллекторское агентство]: §r§cСрок погашения истёк! " +
                            "Весь ваш инвентарь изымается в счёт долга!"), player.getUUID());
            player.sendMessage(new StringTextComponent(""), player.getUUID());

            // Очистить инвентарь игрока
            player.inventory.clearContent();

            // Сброс кредита (принудительно закрыт)
            info.setHasPendingCredit(false);
            info.setCurrentDebt(0);
            info.setInitialCredit(0);
            info.setDaysWithoutPayment(0);
            info.addCreditScore(20); // +20 к плохому рейтингу
            info.setCreditChestPos(Long.MIN_VALUE);
            info.setCreditItemsDescription("");

            player.sendMessage(new StringTextComponent(
                    "§7Рейтинг ухудшен. Новый рейтинг: " + info.getCreditScoreLabel()), player.getUUID());
            data.markChanged();
        }
    }

    private void checkCreditChest(ServerWorld world, ServerPlayerEntity player,
                                  PlayerCreditInfo info, CreditWorldData data,
                                  long gameTick, String dimKey) {
        long chestPos = info.getCreditChestPos();
        if (chestPos == Long.MIN_VALUE) return;

        BlockPos pos = BlockPos.of(chestPos);
        TileEntity te = world.getBlockEntity(pos);
        if (!(te instanceof CreditChestTileEntity)) return;

        CreditChestTileEntity chest = (CreditChestTileEntity) te;
        int diamonds = chest.countDiamonds();
        if (diamonds > 0) {
            chest.clearAllDiamonds();
            CreditCommand.processCreditPayment(player, diamonds);

            if (!info.hasPendingCredit()) {
                // Долг погашен — убрать сундук и вернуть его игроку
                world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                info.setCreditChestPos(Long.MIN_VALUE);
                player.inventory.add(CreditCommand.createCreditChestItem(info));
                data.markChanged();

                // Удалить коллекторов
                world.getEntitiesOfClass(CollectorEntity.class,
                        player.getBoundingBox().inflate(50),
                        c -> c.getTargetPlayerUUID() != null &&
                                c.getTargetPlayerUUID().equals(player.getUUID()))
                        .forEach(c -> c.remove());
            }
        }
    }

    private void removeTaggedItemsFromInventory(ServerPlayerEntity player) {
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (stack.isEmpty()) continue;
            CompoundNBT tag = stack.getTag();
            if (tag != null && (tag.getBoolean("IsCreditContract") || tag.getBoolean("CreditRefusal") || tag.getBoolean("IsCreditChest"))) {
                player.inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private int countDiamondsInInventory(ServerPlayerEntity player) {
        int count = 0;
        for (int i = 0; i < player.inventory.getContainerSize(); i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.DIAMOND) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeDiamondsFromInventory(ServerPlayerEntity player, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.inventory.getContainerSize() && remaining > 0; i++) {
            ItemStack stack = player.inventory.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == Items.DIAMOND) {
                int remove = Math.min(stack.getCount(), remaining);
                stack.shrink(remove);
                remaining -= remove;
                if (stack.isEmpty()) {
                    player.inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }
    }
}
