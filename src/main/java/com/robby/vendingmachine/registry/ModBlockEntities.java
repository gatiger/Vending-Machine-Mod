package com.robby.vendingmachine.registry;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, VendingMachineMod.MOD_ID);

    public static final Supplier<BlockEntityType<VendingMachineBlockEntity>> VENDING_MACHINE =
            BLOCK_ENTITIES.register(
                    "vending_machine",
                    () -> BlockEntityType.Builder.of(
                            VendingMachineBlockEntity::new,
                            ModBlocks.VENDING_MACHINE.get()
                    ).build(null)
            );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}