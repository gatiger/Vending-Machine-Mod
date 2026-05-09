package com.robby.vendingmachine;

import com.mojang.logging.LogUtils;
import com.robby.vendingmachine.registry.ModBlockEntities;
import com.robby.vendingmachine.registry.ModBlocks;
import com.robby.vendingmachine.registry.ModItems;
import com.robby.vendingmachine.registry.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(VendingMachineMod.MOD_ID)
public class VendingMachineMod {
    public static final String MOD_ID = "vendingmachine";
    public static final Logger LOGGER = LogUtils.getLogger();

    public VendingMachineMod(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);

        LOGGER.info("Vending Machine mod loading...");
    }
}