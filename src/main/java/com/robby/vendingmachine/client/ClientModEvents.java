package com.robby.vendingmachine.client;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.robby.vendingmachine.registry.ModBlockEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = VendingMachineMod.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD,
        value = Dist.CLIENT
)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.VENDING_MACHINE_MENU.get(), VendingMachineScreen::new);
        event.register(ModMenus.VENDING_MACHINE_ADMIN_MENU.get(), VendingMachineAdminScreen::new);
        event.register(ModMenus.VENDING_MACHINE_CONFIG_MENU.get(), VendingMachineConfigScreen::new);
        event.register(ModMenus.VENDING_MACHINE_SETTINGS_MENU.get(), VendingMachineSettingsScreen::new);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.VENDING_MACHINE.get(),
                VendingMachineBlockEntityRenderer::new
        );
    }
}