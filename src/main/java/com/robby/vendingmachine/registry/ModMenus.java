package com.robby.vendingmachine.registry;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.menu.VendingMachineAdminMenu;
import com.robby.vendingmachine.menu.VendingMachineConfigMenu;
import com.robby.vendingmachine.menu.VendingMachineMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.robby.vendingmachine.menu.VendingMachineSettingsMenu;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, VendingMachineMod.MOD_ID);

    public static final Supplier<MenuType<VendingMachineMenu>> VENDING_MACHINE_MENU =
            MENUS.register(
                    "vending_machine",
                    () -> IMenuTypeExtension.create(VendingMachineMenu::new)
            );

    public static final Supplier<MenuType<VendingMachineAdminMenu>> VENDING_MACHINE_ADMIN_MENU =
            MENUS.register(
                    "vending_machine_admin",
                    () -> IMenuTypeExtension.create(VendingMachineAdminMenu::new)
            );

    public static final Supplier<MenuType<VendingMachineConfigMenu>> VENDING_MACHINE_CONFIG_MENU =
            MENUS.register(
                    "vending_machine_config",
                    () -> IMenuTypeExtension.create(VendingMachineConfigMenu::new)
            );

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    public static final Supplier<MenuType<VendingMachineSettingsMenu>> VENDING_MACHINE_SETTINGS_MENU =
        MENUS.register("vending_machine_settings",
                () -> IMenuTypeExtension.create(VendingMachineSettingsMenu::new));
}