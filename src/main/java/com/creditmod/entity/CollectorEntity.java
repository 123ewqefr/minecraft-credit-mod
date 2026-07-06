package com.creditmod.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.UUID;

public class CollectorEntity extends MonsterEntity {

    private UUID targetPlayerUUID;

    public CollectorEntity(EntityType<? extends CollectorEntity> type, World world) {
        super(type, world);
        this.setCustomName(new StringTextComponent("§4§l☠ Коллектор ☠")
                .withStyle(Style.EMPTY.withColor(TextFormatting.DARK_RED).withBold(true)));
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeModifierMap.MutableAttribute setAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomWalkingGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(4, new LookRandomlyGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    public boolean hurt(net.minecraft.util.DamageSource source, float amount) {
        // Коллекторы неуязвимы для атак игрока
        if (source.getEntity() instanceof PlayerEntity) {
            ((PlayerEntity) source.getEntity()).sendMessage(
                    new StringTextComponent("§4§l[Коллектор]: §r§cВы не можете навредить нам, должник! Платите долги!"),
                    source.getEntity().getUUID()
            );
            return false;
        }
        return super.hurt(source, amount);
    }

    public void setTargetPlayerUUID(UUID uuid) {
        this.targetPlayerUUID = uuid;
    }

    public UUID getTargetPlayerUUID() {
        return targetPlayerUUID;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }
}
