package com.creditmod.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Хранит все кредитные данные для всего мира (серверная сторона).
 */
public class CreditWorldData extends WorldSavedData {

    private static final String DATA_NAME = "credit_agency_data";

    private final Map<UUID, PlayerCreditInfo> playerData = new HashMap<>();

    public CreditWorldData() {
        super(DATA_NAME);
    }

    /**
     * Получить или создать данные для мира.
     */
    public static CreditWorldData get(World world) {
        if (!(world instanceof ServerWorld)) {
            throw new RuntimeException("CreditWorldData can only be accessed on the server!");
        }
        ServerWorld serverWorld = (ServerWorld) world;
        return serverWorld.getDataStorage().computeIfAbsent(CreditWorldData::new, DATA_NAME);
    }

    /**
     * Получить данные игрока, или создать новые.
     */
    public PlayerCreditInfo getOrCreate(UUID playerId) {
        return playerData.computeIfAbsent(playerId, PlayerCreditInfo::new);
    }

    /**
     * Проверить, есть ли данные об игроке.
     */
    public boolean has(UUID playerId) {
        return playerData.containsKey(playerId);
    }

    public Collection<PlayerCreditInfo> getAllPlayers() {
        return playerData.values();
    }

    @Override
    public void load(CompoundNBT nbt) {
        playerData.clear();
        ListNBT list = nbt.getList("Players", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            PlayerCreditInfo info = PlayerCreditInfo.deserialize(entry);
            playerData.put(info.getPlayerId(), info);
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        ListNBT list = new ListNBT();
        for (PlayerCreditInfo info : playerData.values()) {
            list.add(info.serialize());
        }
        nbt.put("Players", list);
        return nbt;
    }

    /**
     * Пометить данные как изменённые, чтобы они сохранились.
     */
    public void markChanged() {
        setDirty();
    }
}
