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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.function.IntSupplier;

public class VendingMachineAdminMenu extends AbstractContainerMenu {
    public static final int STOCK_SLOT_COUNT = 9;
    public static final int OUTPUT_SLOT_COUNT = 9;
    public static final int CASHBOX_SLOT_COUNT = 9;

    public enum AdminTab {
        STOCK,
        SALES,
        CASHBOX,
        DISPLAY
    }

    private static final int STOCK_TAB_INDEX = AdminTab.STOCK.ordinal();
    private static final int CASHBOX_TAB_INDEX = AdminTab.CASHBOX.ordinal();

    private static final int STOCK_START = 0;
    private static final int OUTPUT_START = STOCK_START + STOCK_SLOT_COUNT;
    private static final int CASHBOX_START = OUTPUT_START + OUTPUT_SLOT_COUNT;
    private static final int PLAYER_INV_START = CASHBOX_START + CASHBOX_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final VendingMachineBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    private int activeTabIndex = STOCK_TAB_INDEX;
    private int syncedSignPreset = 0;

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

    public void setActiveTabIndex(int activeTabIndex) {
        if (activeTabIndex < 0 || activeTabIndex >= AdminTab.values().length) {
            this.activeTabIndex = STOCK_TAB_INDEX;
        } else {
            this.activeTabIndex = activeTabIndex;
        }
    }

    public int getActiveTabIndex() {
        return activeTabIndex;
    }

    public int getSignPreset() {
        return syncedSignPreset;
    }

    private boolean isTabActive(int tabIndex) {
        return this.activeTabIndex == tabIndex;
    }

    private boolean shouldPlayerInventoryBeActive() {
        return isTabActive(STOCK_TAB_INDEX) || isTabActive(CASHBOX_TAB_INDEX);
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
            this.addSlot(new TabbedHighStackSlotItemHandler(
                    stockInventory,
                    slot,
                    startX + slot * 18,
                    startY,
                    () -> this.activeTabIndex,
                    STOCK_TAB_INDEX
            ));
        }
    }

    private void addOutputTraySlots(ItemStackHandler outputInventory) {
        int startX = 8;
        int startY = 136;

        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            this.addSlot(new TabbedSlotItemHandler(
                    outputInventory,
                    slot,
                    startX + slot * 18,
                    startY,
                    () -> this.activeTabIndex,
                    STOCK_TAB_INDEX
            ));
        }
    }

    private void addCashboxSlots(ItemStackHandler cashboxInventory) {
        int startX = 8;
        int startY = 78;

        for (int slot = 0; slot < CASHBOX_SLOT_COUNT; slot++) {
            this.addSlot(new TabbedHighStackSlotItemHandler(
                    cashboxInventory,
                    slot,
                    startX + slot * 18,
                    startY,
                    () -> this.activeTabIndex,
                    CASHBOX_TAB_INDEX
            ));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int startX = 8;
        int startY = 240;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new TabbedPlayerSlot(
                        playerInventory,
                        column + row * 9 + 9,
                        startX + column * 18,
                        startY + row * 18,
                        this::shouldPlayerInventoryBeActive
                ));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        int startX = 8;
        int startY = 298;

        for (int column = 0; column < 9; column++) {
            this.addSlot(new TabbedPlayerSlot(
                    playerInventory,
                    column,
                    startX + column * 18,
                    startY,
                    this::shouldPlayerInventoryBeActive
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

        if (buttonId == 1) {
            blockEntity.cycleSignPreset(-1);
            return true;
        }

        if (buttonId == 2) {
            blockEntity.cycleSignPreset(1);
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

        if (quickMovedSlot == null || !quickMovedSlot.hasItem() || !quickMovedSlot.isActive()) {
            return ItemStack.EMPTY;
        }

        ItemStack rawStack = quickMovedSlot.getItem();
        quickMovedStack = rawStack.copy();

        if (isAdminSlot(quickMovedSlotIndex)) {
            if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (isPlayerSlot(quickMovedSlotIndex)) {
            if (isTabActive(STOCK_TAB_INDEX)) {
                if (!this.moveItemStackTo(rawStack, STOCK_START, OUTPUT_START, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isTabActive(CASHBOX_TAB_INDEX)) {
                if (!this.moveItemStackTo(rawStack, CASHBOX_START, PLAYER_INV_START, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
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

        return quickMovedStack;
    }

    private boolean isAdminSlot(int index) {
        if (isTabActive(STOCK_TAB_INDEX)) {
            return index >= STOCK_START && index < CASHBOX_START;
        }

        if (isTabActive(CASHBOX_TAB_INDEX)) {
            return index >= CASHBOX_START && index < PLAYER_INV_START;
        }

        return false;
    }

    private boolean isPlayerSlot(int index) {
        return index >= PLAYER_INV_START && index < HOTBAR_END;
    }

    public VendingMachineBlockEntity getBlockEntity() {
        return blockEntity;
    }

    private static class TabbedSlotItemHandler extends SlotItemHandler {
        private final IntSupplier activeTabSupplier;
        private final int visibleTabIndex;

        public TabbedSlotItemHandler(
                ItemStackHandler itemHandler,
                int index,
                int xPosition,
                int yPosition,
                IntSupplier activeTabSupplier,
                int visibleTabIndex
        ) {
            super(itemHandler, index, xPosition, yPosition);
            this.activeTabSupplier = activeTabSupplier;
            this.visibleTabIndex = visibleTabIndex;
        }

        @Override
        public boolean isActive() {
            return activeTabSupplier.getAsInt() == visibleTabIndex;
        }
    }

    private static class TabbedHighStackSlotItemHandler extends TabbedSlotItemHandler {
        public TabbedHighStackSlotItemHandler(
                ItemStackHandler itemHandler,
                int index,
                int xPosition,
                int yPosition,
                IntSupplier activeTabSupplier,
                int visibleTabIndex
        ) {
            super(itemHandler, index, xPosition, yPosition, activeTabSupplier, visibleTabIndex);
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

    private static class TabbedPlayerSlot extends Slot {
        private final ActiveCheck activeCheck;

        public TabbedPlayerSlot(Inventory inventory, int index, int x, int y, ActiveCheck activeCheck) {
            super(inventory, index, x, y);
            this.activeCheck = activeCheck;
        }

        @Override
        public boolean isActive() {
            return activeCheck.isActive();
        }
    }

    @FunctionalInterface
    private interface ActiveCheck {
        boolean isActive();
    }
}