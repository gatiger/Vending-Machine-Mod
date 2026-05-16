package com.robby.vendingmachine.menu;

import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import com.robby.vendingmachine.registry.ModBlocks;
import com.robby.vendingmachine.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

public class VendingMachineMenu extends AbstractContainerMenu {
    public static final int SALE_SLOT_COUNT = VendingMachineBlockEntity.SALE_SLOT_COUNT;
    public static final int DISPLAY_CONFIG_SLOT_COUNT = VendingMachineBlockEntity.CONFIG_SLOTS;
    public static final int OUTPUT_SLOT_COUNT = 9;

    private static final int DISPLAY_CONFIG_START = 0;
    private static final int OUTPUT_START = DISPLAY_CONFIG_START + DISPLAY_CONFIG_SLOT_COUNT;
    private static final int PLAYER_INV_START = OUTPUT_START + OUTPUT_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final VendingMachineBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    private final int[] syncedStockCounts = new int[SALE_SLOT_COUNT];

    public VendingMachineMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData.readBlockPos()));
    }

    public VendingMachineMenu(int containerId, Inventory playerInventory, VendingMachineBlockEntity blockEntity) {
        super(ModMenus.VENDING_MACHINE_MENU.get(), containerId);

        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        for (int saleIndex = 0; saleIndex < SALE_SLOT_COUNT; saleIndex++) {
            final int index = saleIndex;

            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
                        return blockEntity.getConfiguredSaleStockCount(index);
                    }

                    return syncedStockCounts[index];
                }

                @Override
                public void set(int value) {
                    syncedStockCounts[index] = value;
                }
            });
        }

        addDisplayConfigSlots(blockEntity.getConfigInventory());
        addOutputTraySlots(blockEntity.getOutputInventory());
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

    private void addDisplayConfigSlots(ItemStackHandler configInventory) {
        int startX = 14;
        int startY = 34;

        for (int saleIndex = 0; saleIndex < SALE_SLOT_COUNT; saleIndex++) {
            int row = saleIndex / 3;
            int col = saleIndex % 3;

            int x = startX + col * 54;
            int y = startY + row * 44;

            this.addSlot(new DisplayOnlySlot(
                    configInventory,
                    VendingMachineBlockEntity.getSellConfigSlot(saleIndex),
                    x,
                    y
            ));

            this.addSlot(new DisplayOnlySlot(
                    configInventory,
                    VendingMachineBlockEntity.getPriceConfigSlot(saleIndex),
                    x + 24,
                    y
            ));
        }
    }

    private void addOutputTraySlots(ItemStackHandler outputInventory) {
        int startX = 8;
        int startY = 186;

        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            this.addSlot(new OutputOnlySlot(outputInventory, slot, startX + slot * 18, startY));
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
        System.out.println("Shop Buy button clicked. Sale slot: " + buttonId);

        if (buttonId >= 0 && buttonId < SALE_SLOT_COUNT) {
            boolean result = blockEntity.tryBuyConfiguredItem(player, buttonId);
            System.out.println("Purchase result for sale slot " + buttonId + ": " + result);
            return result;
        }

        return false;
    }

    public int getConfiguredSaleStockCount(int saleIndex) {
        if (saleIndex < 0 || saleIndex >= SALE_SLOT_COUNT) {
            return 0;
        }

        return syncedStockCounts[saleIndex];
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

            // Display config slots and output tray -> player inventory/hotbar.
            // Display config slots are not pickup-able, so this mostly affects output tray.
            if (quickMovedSlotIndex >= DISPLAY_CONFIG_START && quickMovedSlotIndex < PLAYER_INV_START) {
                if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (quickMovedSlotIndex >= PLAYER_INV_START && quickMovedSlotIndex < PLAYER_INV_END) {
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

    private static class DisplayOnlySlot extends SlotItemHandler {
        public DisplayOnlySlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
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

    private static class OutputOnlySlot extends SlotItemHandler {
        public OutputOnlySlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}