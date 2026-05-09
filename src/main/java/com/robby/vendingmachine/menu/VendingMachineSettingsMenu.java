package com.robby.vendingmachine.menu;

import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import com.robby.vendingmachine.registry.ModBlocks;
import com.robby.vendingmachine.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VendingMachineSettingsMenu extends AbstractContainerMenu {
    private final VendingMachineBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    private int syncedSignPreset = 0;

    public VendingMachineSettingsMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData.readBlockPos()));
    }

    public VendingMachineSettingsMenu(int containerId, Inventory playerInventory, VendingMachineBlockEntity blockEntity) {
        super(ModMenus.VENDING_MACHINE_SETTINGS_MENU.get(), containerId);

        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
                    return blockEntity.getSignPreset();
                }

                return syncedSignPreset;
            }

            @Override
            public void set(int value) {
                syncedSignPreset = value;
            }
        });
    }

    private static VendingMachineBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);

        if (blockEntity instanceof VendingMachineBlockEntity vendingMachineBlockEntity) {
            return vendingMachineBlockEntity;
        }

        throw new IllegalStateException("Expected VendingMachineBlockEntity at " + pos);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0) {
            blockEntity.cycleSignPreset(-1);
            return true;
        }

        if (buttonId == 1) {
            blockEntity.cycleSignPreset(1);
            return true;
        }

        if (buttonId == 999 && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, playerInventory, openedPlayer) ->
                                    new VendingMachineAdminMenu(containerId, playerInventory, blockEntity),
                            Component.translatable("menu.vendingmachine.vending_machine_admin")
                    ),
                    buffer -> buffer.writeBlockPos(blockEntity.getBlockPos())
            );

            return true;
        }

        return false;
    }

    public int getSignPreset() {
        return syncedSignPreset;
    }

    public VendingMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.VENDING_MACHINE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        return ItemStack.EMPTY;
    }
}