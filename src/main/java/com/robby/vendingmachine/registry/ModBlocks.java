package com.robby.vendingmachine.registry;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.block.VendingMachineBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.robby.vendingmachine.item.VendingMachineBlockItem;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(VendingMachineMod.MOD_ID);

    public static final DeferredBlock<VendingMachineBlock> VENDING_MACHINE = BLOCKS.register(
            "vending_machine",
            () -> new VendingMachineBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.METAL)
                            .strength(1.2F, 6.0F)
                            .sound(SoundType.METAL)
                            .noOcclusion()
                            .requiresCorrectToolForDrops()
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);

        ModItems.ITEMS.register(
                "vending_machine",
                () -> new VendingMachineBlockItem(
                        VENDING_MACHINE.get(),
                        new Item.Properties()
                )
        );
    }
}