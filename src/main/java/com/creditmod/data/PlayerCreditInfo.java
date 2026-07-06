package com.creditmod.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;

import java.util.UUID;

/**
 * Хранит информацию о кредите одного игрока.
 */
public class PlayerCreditInfo {

    private UUID playerId;

    // Кредитная история (score). Начальное значение — 300 (плохо).
    // Чем ниже — тем лучше. >400 — кредиты не выдаются.
    private int creditScore = 300;

    // Текущий долг в алмазах (дробное, т.к. накапливаются проценты)
    private double currentDebt = 0.0;

    // Изначальная сумма кредита
    private double initialCredit = 0.0;

    // Тик последнего платежа (или взятия кредита)
    private long lastPaymentTick = 0;

    // Тик взятия кредита
    private long creditTakenTick = 0;

    // Тик последнего начисления процентов
    private long lastInterestTick = 0;

    // Тик последнего появления коллекторов
    private long lastCollectorSpawnTick = 0;

    // Есть ли активный кредит
    private boolean hasPendingCredit = false;

    // Количество полных платежей за текущий кредит
    private int paymentsThisCredit = 0;

    // Сколько игровых дней прошло без платежа (отсчитывается от lastPaymentTick)
    private int daysWithoutPayment = 0;

    // Позиция кредитного сундука (если установлен). -1 = не установлен
    private long creditChestPos = Long.MIN_VALUE;

    // Описание товаров в кредите (для /credit history)
    private String creditItemsDescription = "";

    public PlayerCreditInfo(UUID id) {
        this.playerId = id;
    }

    // === Getters / Setters ===

    public UUID getPlayerId() { return playerId; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int s) { this.creditScore = Math.max(0, s); }
    public void addCreditScore(int delta) { this.creditScore = Math.max(0, creditScore + delta); }

    public double getCurrentDebt() { return currentDebt; }
    public void setCurrentDebt(double d) { this.currentDebt = Math.max(0, d); }

    public double getInitialCredit() { return initialCredit; }
    public void setInitialCredit(double v) { this.initialCredit = v; }

    public long getLastPaymentTick() { return lastPaymentTick; }
    public void setLastPaymentTick(long t) { this.lastPaymentTick = t; }

    public long getCreditTakenTick() { return creditTakenTick; }
    public void setCreditTakenTick(long t) { this.creditTakenTick = t; }

    public long getLastInterestTick() { return lastInterestTick; }
    public void setLastInterestTick(long t) { this.lastInterestTick = t; }

    public long getLastCollectorSpawnTick() { return lastCollectorSpawnTick; }
    public void setLastCollectorSpawnTick(long t) { this.lastCollectorSpawnTick = t; }

    public boolean hasPendingCredit() { return hasPendingCredit; }
    public void setHasPendingCredit(boolean v) { this.hasPendingCredit = v; }

    public int getPaymentsThisCredit() { return paymentsThisCredit; }
    public void incrementPayments() { paymentsThisCredit++; }

    public int getDaysWithoutPayment() { return daysWithoutPayment; }
    public void setDaysWithoutPayment(int d) { this.daysWithoutPayment = d; }

    public long getCreditChestPos() { return creditChestPos; }
    public void setCreditChestPos(long pos) { this.creditChestPos = pos; }

    public String getCreditItemsDescription() { return creditItemsDescription; }
    public void setCreditItemsDescription(String d) { this.creditItemsDescription = d; }

    /**
     * Процентная ставка в минуту, зависит от кредитного рейтинга.
     */
    public double getInterestRatePerMinute() {
        if (creditScore >= 300) return 0.10;
        if (creditScore >= 200) return 0.08;
        if (creditScore >= 100) return 0.06;
        return 0.03;
    }

    /**
     * Срок на погашение в игровых днях (базово). 
     * Для "плохой" истории 1 предмет = 1 день, до 5 дней максимум.
     */
    public int getRepaymentDays() {
        if (creditScore >= 300) return 5;
        if (creditScore >= 200) return 10;
        if (creditScore >= 100) return 20;
        return 40;
    }

    /**
     * Уровень кредитной истории в виде строки.
     */
    public String getCreditScoreLabel() {
        if (creditScore > 400) return "§4Заблокирован (>400)";
        if (creditScore >= 300) return "§c300+ — Плохой";
        if (creditScore >= 200) return "§6200 — Неплохой";
        if (creditScore >= 100) return "§a100 — Хороший";
        return "§b≤50 — Очень хороший";
    }

    // === NBT сериализация ===

    public CompoundNBT serialize() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putUUID("PlayerId", playerId);
        nbt.putInt("CreditScore", creditScore);
        nbt.putDouble("CurrentDebt", currentDebt);
        nbt.putDouble("InitialCredit", initialCredit);
        nbt.putLong("LastPaymentTick", lastPaymentTick);
        nbt.putLong("CreditTakenTick", creditTakenTick);
        nbt.putLong("LastInterestTick", lastInterestTick);
        nbt.putLong("LastCollectorSpawnTick", lastCollectorSpawnTick);
        nbt.putBoolean("HasPendingCredit", hasPendingCredit);
        nbt.putInt("PaymentsThisCredit", paymentsThisCredit);
        nbt.putInt("DaysWithoutPayment", daysWithoutPayment);
        nbt.putLong("CreditChestPos", creditChestPos);
        nbt.putString("CreditItemsDescription", creditItemsDescription);
        return nbt;
    }

    public static PlayerCreditInfo deserialize(CompoundNBT nbt) {
        UUID id = nbt.getUUID("PlayerId");
        PlayerCreditInfo info = new PlayerCreditInfo(id);
        info.creditScore = nbt.getInt("CreditScore");
        info.currentDebt = nbt.getDouble("CurrentDebt");
        info.initialCredit = nbt.getDouble("InitialCredit");
        info.lastPaymentTick = nbt.getLong("LastPaymentTick");
        info.creditTakenTick = nbt.getLong("CreditTakenTick");
        info.lastInterestTick = nbt.getLong("LastInterestTick");
        info.lastCollectorSpawnTick = nbt.getLong("LastCollectorSpawnTick");
        info.hasPendingCredit = nbt.getBoolean("HasPendingCredit");
        info.paymentsThisCredit = nbt.getInt("PaymentsThisCredit");
        info.daysWithoutPayment = nbt.getInt("DaysWithoutPayment");
        info.creditChestPos = nbt.getLong("CreditChestPos");
        info.creditItemsDescription = nbt.getString("CreditItemsDescription");
        return info;
    }
}
