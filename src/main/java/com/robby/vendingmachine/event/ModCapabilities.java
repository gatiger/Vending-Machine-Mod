package com.robby.vendingmachine.event;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.block.VendingMachineBlock;
import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import com.robby.vendingmachine.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;

@EventBusSubscriber(
        modid = VendingMachineMod.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD
)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> {
                    if (!(state.getBlock() instanceof VendingMachineBlock)) {
                        return null;
                    }

                    BlockPos lowerPos = state.getValue(VendingMachineBlock.HALF) == DoubleBlockHalf.UPPER
                            ? pos.below()
                            : pos;

                    BlockEntity lowerBlockEntity = level.getBlockEntity(lowerPos);

                    if (lowerBlockEntity instanceof VendingMachineBlockEntity vendingMachineBlockEntity) {
                        return vendingMachineBlockEntity.getAutomationItemHandler();
                    }

                    return null;
                },
                ModBlocks.VENDING_MACHINE.get()
        );
    }
}