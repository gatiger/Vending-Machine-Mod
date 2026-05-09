package com.robby.vendingmachine.event;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.registry.ModBlocks;
import com.robby.vendingmachine.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(
        modid = VendingMachineMod.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD
)
public class ModCreativeTabEvents {
    @SubscribeEvent
    public static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.VENDING_MACHINE.get().asItem());
        }

        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.ADMIN_TOOL.get());
        }

        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.VENDOR_COIN.get());
        }
    }
}