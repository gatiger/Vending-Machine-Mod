package com.robby.vendingmachine.menu;

import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import com.robby.vendingmachine.registry.ModBlocks;
import com.robby.vendingmachine.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
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
    public static final int SALE_SLOT_COUNT = VendingMachineBlockEntity.SALE_SLOT_COUNT;
    public static final int GHOST_SLOT_COUNT = VendingMachineBlockEntity.CONFIG_SLOTS;

    public static final int SIGN_PREVIOUS_BUTTON_ID = 1;
    public static final int SIGN_NEXT_BUTTON_ID = 2;
    public static final int TAB_BUTTON_OFFSET = 900;
    public static final int SALES_BUTTON_OFFSET = 1000;

    public enum AdminTab {
        STOCK,
        SALES,
        CASHBOX,
        DISPLAY
    }

    private static final int STOCK_TAB_INDEX = AdminTab.STOCK.ordinal();
    private static final int SALES_TAB_INDEX = AdminTab.SALES.ordinal();
    private static final int CASHBOX_TAB_INDEX = AdminTab.CASHBOX.ordinal();

    private static final int STOCK_START = 0;
    private static final int OUTPUT_START = STOCK_START + STOCK_SLOT_COUNT;
    private static final int CASHBOX_START = OUTPUT_START + OUTPUT_SLOT_COUNT;
    private static final int GHOST_START = CASHBOX_START + CASHBOX_SLOT_COUNT;
    private static final int PLAYER_INV_START = GHOST_START + GHOST_SLOT_COUNT;
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
        addGhostSlots(blockEntity.getConfigInventory());
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
        return isTabActive(STOCK_TAB_INDEX)
                || isTabActive(SALES_TAB_INDEX)
                || isTabActive(CASHBOX_TAB_INDEX);
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

    private void addGhostSlots(ItemStackHandler configInventory) {
        int startX = 8;
        int startY = 30;

        for (int saleIndex = 0; saleIndex < SALE_SLOT_COUNT; saleIndex++) {
            int y = startY + saleIndex * 26;

            this.addSlot(new GhostSlot(
                    configInventory,
                    VendingMachineBlockEntity.getSellConfigSlot(saleIndex),
                    startX,
                    y,
                    () -> this.activeTabIndex,
                    SALES_TAB_INDEX
            ));

            this.addSlot(new GhostSlot(
                    configInventory,
                    VendingMachineBlockEntity.getPriceConfigSlot(saleIndex),
                    startX + 84,
                    y,
                    () -> this.activeTabIndex,
                    SALES_TAB_INDEX
            ));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int startX = 8;
        int startY = 290;

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
        int startY = 348;

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
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= GHOST_START && slotId < PLAYER_INV_START) {
            Slot slot = this.slots.get(slotId);

            if (slot instanceof GhostSlot ghostSlot && ghostSlot.isActive()) {
                handleGhostSlotClick(ghostSlot.getSlotIndex());
            }

            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    private void handleGhostSlotClick(int configSlot) {
        ItemStack carried = this.getCarried();
        ItemStackHandler configInventory = blockEntity.getConfigInventory();

        if (carried.isEmpty()) {
            configInventory.setStackInSlot(configSlot, ItemStack.EMPTY);
            return;
        }

        if (carried.is(ModBlocks.VENDING_MACHINE.get().asItem())) {
            return;
        }

        ItemStack existing = configInventory.getStackInSlot(configSlot);
        int quantity = existing.isEmpty() ? 1 : Math.max(1, existing.getCount());

        ItemStack template = carried.copy();
        template.setCount(quantity);

        configInventory.setStackInSlot(configSlot, template);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId >= TAB_BUTTON_OFFSET && buttonId < TAB_BUTTON_OFFSET + AdminTab.values().length) {
            setActiveTabIndex(buttonId - TAB_BUTTON_OFFSET);
            return true;
        }

        if (buttonId == SIGN_PREVIOUS_BUTTON_ID) {
            blockEntity.cycleSignPreset(-1);
            return true;
        }

        if (buttonId == SIGN_NEXT_BUTTON_ID) {
            blockEntity.cycleSignPreset(1);
            return true;
        }

        if (buttonId >= SALES_BUTTON_OFFSET) {
            return handleSalesQuantityButton(buttonId - SALES_BUTTON_OFFSET);
        }

        return false;
    }

    private boolean handleSalesQuantityButton(int localButtonId) {
        int saleIndex = localButtonId / 12;
        int buttonWithinSale = localButtonId % 12;

        if (saleIndex < 0 || saleIndex >= SALE_SLOT_COUNT) {
            return false;
        }

        boolean priceSlot = buttonWithinSale >= 6;
        int action = buttonWithinSale % 6;

        int configSlot = priceSlot
                ? VendingMachineBlockEntity.getPriceConfigSlot(saleIndex)
                : VendingMachineBlockEntity.getSellConfigSlot(saleIndex);

        int delta = switch (action) {
            case 0 -> 1;
            case 1 -> -1;
            case 2 -> 16;
            case 3 -> -16;
            case 4 -> 64;
            case 5 -> -64;
            default -> 0;
        };

        if (delta == 0) {
            return false;
        }

        return adjustGhostQuantity(configSlot, delta);
    }

    private boolean adjustGhostQuantity(int configSlot, int delta) {
        ItemStackHandler configInventory = blockEntity.getConfigInventory();
        ItemStack current = configInventory.getStackInSlot(configSlot);

        if (current.isEmpty()) {
            return false;
        }

        int currentCount = current.getCount();
        int newCount;

        if (currentCount == 1 && delta > 1) {
            newCount = delta;
        } else {
            newCount = currentCount + delta;
        }

        ItemStack updated = current.copy();
        updated.setCount(Mth.clamp(newCount, 1, 999));

        configInventory.setStackInSlot(configSlot, updated);
        return true;
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
                if (!this.moveItemStackTo(rawStack, CASHBOX_START, GHOST_START, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (isTabActive(SALES_TAB_INDEX)) {
                if (quickMovedSlotIndex >= PLAYER_INV_START && quickMovedSlotIndex < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(rawStack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (quickMovedSlotIndex >= HOTBAR_START && quickMovedSlotIndex < HOTBAR_END) {
                    if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
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
            return index >= CASHBOX_START && index < GHOST_START;
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

    private static class GhostSlot extends TabbedSlotItemHandler {
        public GhostSlot(
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
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
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