package com.creditmod.registry;

import com.creditmod.CreditMod;
import com.creditmod.entity.CollectorEntity;
import com.creditmod.entity.CreditAgencyEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITIES, CreditMod.MOD_ID);

    public static final RegistryObject<EntityType<CreditAgencyEntity>> CREDIT_AGENCY =
            ENTITIES.register("credit_agency",
                    () -> EntityType.Builder.<CreditAgencyEntity>of(CreditAgencyEntity::new, EntityClassification.CREATURE)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("creditmod:credit_agency"));

    public static final RegistryObject<EntityType<CollectorEntity>> COLLECTOR =
            ENTITIES.register("collector",
                    () -> EntityType.Builder.<CollectorEntity>of(CollectorEntity::new, EntityClassification.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("creditmod:collector"));

    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(CREDIT_AGENCY.get(), CreditAgencyEntity.setAttributes().build());
        event.put(COLLECTOR.get(), CollectorEntity.setAttributes().build());
    }
}
