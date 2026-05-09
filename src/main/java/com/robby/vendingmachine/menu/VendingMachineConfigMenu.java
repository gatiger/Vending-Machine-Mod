package com.robby.vendingmachine.menu;

import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import com.robby.vendingmachine.registry.ModBlocks;
import com.robby.vendingmachine.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import com.robby.vendingmachine.registry.ModBlocks;

public class VendingMachineConfigMenu extends AbstractContainerMenu {
    public static final int SALE_SLOT_COUNT = VendingMachineBlockEntity.SALE_SLOT_COUNT;
    public static final int GHOST_SLOT_COUNT = VendingMachineBlockEntity.CONFIG_SLOTS;
    public static final int BACK_BUTTON_ID = SALE_SLOT_COUNT * 12;

    private static final int GHOST_START = 0;
    private static final int PLAYER_INV_START = GHOST_START + GHOST_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final VendingMachineBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public VendingMachineConfigMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, getBlockEntity(playerInventory, extraData.readBlockPos()));
    }

    public VendingMachineConfigMenu(int containerId, Inventory playerInventory, VendingMachineBlockEntity blockEntity) {
        super(ModMenus.VENDING_MACHINE_CONFIG_MENU.get(), containerId);

        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        addGhostSlots(blockEntity.getConfigInventory());
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

    private void addGhostSlots(ItemStackHandler configInventory) {
        int startX = 8;
        int startY = 30;

        for (int saleIndex = 0; saleIndex < SALE_SLOT_COUNT; saleIndex++) {
            int y = startY + saleIndex * 26;

            this.addSlot(new GhostSlot(
                    configInventory,
                    VendingMachineBlockEntity.getSellConfigSlot(saleIndex),
                    startX,
                    y
            ));

            this.addSlot(new GhostSlot(
                    configInventory,
                    VendingMachineBlockEntity.getPriceConfigSlot(saleIndex),
                    startX + 84,
                    y
            ));
        }
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int startX = 8;
        int startY = 290;

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
        int startY = 348;

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
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= GHOST_START && slotId < PLAYER_INV_START) {
            handleGhostSlotClick(slotId);
            return;
        }

        super.clicked(slotId, button, clickType, player);
    }

    private void handleGhostSlotClick(int slotId) {
        ItemStack carried = this.getCarried();
        ItemStackHandler configInventory = blockEntity.getConfigInventory();

        if (carried.isEmpty()) {
            configInventory.setStackInSlot(slotId, ItemStack.EMPTY);
            return;
        }

        // Prevent vending machines from being used as sell/price templates.
        if (carried.is(ModBlocks.VENDING_MACHINE.get().asItem())) {
            return;
        }

        ItemStack existing = configInventory.getStackInSlot(slotId);
        int quantity = existing.isEmpty() ? 1 : Math.max(1, existing.getCount());

        ItemStack template = carried.copy();
        template.setCount(quantity);

        configInventory.setStackInSlot(slotId, template);
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == BACK_BUTTON_ID) {
            return false;
        }

    int saleIndex = buttonId / 12;
    int buttonWithinSale = buttonId % 12;

        if (saleIndex < 0 || saleIndex >= SALE_SLOT_COUNT) {
            return false;
        }

        boolean priceSlot = buttonWithinSale >= 6;
        int action = buttonWithinSale % 6;

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

        int configSlot = priceSlot
                ? VendingMachineBlockEntity.getPriceConfigSlot(saleIndex)
                : VendingMachineBlockEntity.getSellConfigSlot(saleIndex);

        return adjustGhostQuantity(configSlot, delta);
    }

    public boolean adjustPriceQuantity(Player player, int buttonId) {
        return false;
    }

    public boolean adjustGhostQuantityByAction(int saleIndex, boolean priceSlot, int action) {
        if (saleIndex < 0 || saleIndex >= SALE_SLOT_COUNT) {
            return false;
        }

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

        // Treat the first ghost item as a template.
        // Shift+Click should become 16, not 17.
        // Ctrl+Shift+Click should become 64, not 65.
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

    private boolean openAdminMenu(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
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

    private static class GhostSlot extends SlotItemHandler {
        public GhostSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
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
}