package com.creditmod.entity;

import com.creditmod.data.CreditWorldData;
import com.creditmod.data.PlayerCreditInfo;
import com.creditmod.gui.CreditShopContainer;
import com.creditmod.registry.ModContainers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class CreditAgencyEntity extends MobEntity {

    private static final ITextComponent DISPLAY_NAME = new StringTextComponent("Кредитное агентство")
            .withStyle(Style.EMPTY.withColor(TextFormatting.GOLD).withBold(true));

    public CreditAgencyEntity(EntityType<? extends CreditAgencyEntity> type, World world) {
        super(type, world);
        this.setCustomName(DISPLAY_NAME);
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
    }

    public static AttributeModifierMap.MutableAttribute setAttributes() {
        return MobEntity.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(2, new LookRandomlyGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.5D));
    }

    @Override
    public ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (!this.level.isClientSide && player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            // Check if player has active credit — inform them
            if (!this.level.isClientSide) {
                CreditWorldData data = CreditWorldData.get(this.level);
                PlayerCreditInfo info = data.getOrCreate(player.getUUID());

                if (info.hasPendingCredit()) {
                    sendNpcMessage(player, "§6§lКредитное агентство: §r§eУ вас уже есть активный кредит! " +
                            "Сначала погасите текущий долг.");
                    return ActionResultType.CONSUME;
                }

                // Open credit shop
                final CreditAgencyEntity self = this;
                NetworkHooks.openGui(serverPlayer, new INamedContainerProvider() {
                    @Override
                    public ITextComponent getDisplayName() {
                        return DISPLAY_NAME;
                    }

                    @Nullable
                    @Override
                    public Container createMenu(int id, PlayerInventory inv, PlayerEntity p) {
                        return new CreditShopContainer(id, inv, self.getId());
                    }
                }, buf -> buf.writeInt(self.getId()));

                sendNpcMessage(player, "§6§lКредитное агентство: §r§eДобро пожаловать в наше кредитное агентство! " +
                        "Нажмите на меня ПКМ, чтобы открыть меню кредитования.");
            }
        }
        return ActionResultType.sidedSuccess(this.level.isClientSide);
    }

    @Override
    public boolean hurt(net.minecraft.util.DamageSource source, float amount) {
        // Неуязвим для атак игрока
        if (source.getEntity() instanceof PlayerEntity) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    public static void sendNpcMessage(PlayerEntity player, String msg) {
        player.sendMessage(new StringTextComponent(msg), player.getUUID());
    }
}
