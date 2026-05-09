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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class VendingMachineAdminMenu extends AbstractContainerMenu {
    public static final int STOCK_SLOT_COUNT = 9;
    public static final int OUTPUT_SLOT_COUNT = 9;
    public static final int CASHBOX_SLOT_COUNT = 9;

    private static final int STOCK_START = 0;
    private static final int OUTPUT_START = STOCK_START + STOCK_SLOT_COUNT;
    private static final int CASHBOX_START = OUTPUT_START + OUTPUT_SLOT_COUNT;
    private static final int PLAYER_INV_START = CASHBOX_START + CASHBOX_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final VendingMachineBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public VendingMachineAdminMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData.readBlockPos()));
    }

    public VendingMachineAdminMenu(int containerId, Inventory playerInventory, VendingMachineBlockEntity blockEntity) {
        super(ModMenus.VENDING_MACHINE_ADMIN_MENU.get(), containerId);

        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addStockSlots(blockEntity.getStockInventory());
        addOutputTraySlots(blockEntity.getOutputInventory());
        addCashboxSlots(blockEntity.getCashboxInventory());
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    private static VendingMachineBlockEntity getBlockEntity(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);

        if (blockEntity instanceof VendingMachineBlockEntity vendingMachineBlockEntity) {
            return vendingMachineBlockEntity;
        }

        throw new IllegalStateException("Expected VendingMachineBlockEntity at " + pos);
    }

    private void addStockSlots(ItemStackHandler stockInventory) {
        int startX = 8;
        int startY = 78;

        for (int slot = 0; slot < STOCK_SLOT_COUNT; slot++) {
            this.addSlot(new HighStackSlotItemHandler(stockInventory, slot, startX + slot * 18, startY));
        }
    }

    private void addOutputTraySlots(ItemStackHandler outputInventory) {
        int startX = 8;
        int startY = 136;

        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            this.addSlot(new SlotItemHandler(outputInventory, slot, startX + slot * 18, startY));
        }
    }

    private void addCashboxSlots(ItemStackHandler cashboxInventory) {
        int startX = 8;
        int startY = 194;

        for (int slot = 0; slot < CASHBOX_SLOT_COUNT; slot++) {
            this.addSlot(new HighStackSlotItemHandler(cashboxInventory, slot, startX + slot * 18, startY));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int startX = 8;
        int startY = 240;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        startX + column * 18,
                        startY + row * 18
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        int startX = 8;
        int startY = 298;

        for (int column = 0; column < 9; column++) {
            this.addSlot(new Slot(
                    playerInventory,
                    column,
                    startX + column * 18,
                    startY
            ));
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0 && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, playerInventory, openedPlayer) ->
                                    new VendingMachineConfigMenu(containerId, playerInventory, blockEntity),
                            Component.translatable("menu.vendingmachine.vending_machine_config")
                    ),
                    buffer -> buffer.writeBlockPos(blockEntity.getBlockPos())
            );

            return true;
        }

        if (buttonId == 1 && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, playerInventory, openedPlayer) ->
                                    new VendingMachineSettingsMenu(containerId, playerInventory, blockEntity),
                            Component.literal("Vending Machine Settings")
                    ),
                    buffer -> buffer.writeBlockPos(blockEntity.getBlockPos())
            );

            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.VENDING_MACHINE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            // Admin inventories -> player inventory/hotbar
            if (quickMovedSlotIndex >= STOCK_START && quickMovedSlotIndex < PLAYER_INV_START) {
                if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Player inventory/hotbar -> stock inventory by default
            else if (quickMovedSlotIndex >= PLAYER_INV_START && quickMovedSlotIndex < HOTBAR_END) {
                if (!this.moveItemStackTo(rawStack, STOCK_START, OUTPUT_START, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (rawStack.isEmpty()) {
                quickMovedSlot.set(ItemStack.EMPTY);
            } else {
                quickMovedSlot.setChanged();
            }

            if (rawStack.getCount() == quickMovedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack;
    }

    public VendingMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private static class HighStackSlotItemHandler extends SlotItemHandler {
        public HighStackSlotItemHandler(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public int getMaxStackSize() {
            return this.getItemHandler().getSlotLimit(this.getSlotIndex());
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return this.getItemHandler().getSlotLimit(this.getSlotIndex());
        }
    }
}