package com.robby.vendingmachine.event;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(
        modid = VendingMachineMod.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD
)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.VENDING_MACHINE.get(),
                (blockEntity, side) -> blockEntity.getAutomationItemHandler()
        );
    }
}