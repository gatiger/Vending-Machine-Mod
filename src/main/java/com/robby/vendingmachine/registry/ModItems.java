package com.robby.vendingmachine.registry;

import com.robby.vendingmachine.VendingMachineMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(VendingMachineMod.MOD_ID);

    public static final DeferredItem<Item> ADMIN_TOOL = ITEMS.registerSimpleItem(
            "admin_tool",
            new Item.Properties().stacksTo(1)
    );

    public static final DeferredItem<Item> VENDOR_COIN = ITEMS.registerSimpleItem(
            "vendor_coin",
            new Item.Properties()
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}