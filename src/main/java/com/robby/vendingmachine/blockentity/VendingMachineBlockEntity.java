package com.robby.vendingmachine.blockentity;

import com.robby.vendingmachine.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import com.robby.vendingmachine.registry.ModBlocks;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;

import java.util.UUID;

public class VendingMachineBlockEntity extends BlockEntity {
    public static final int SALE_SLOT_COUNT = 9;
    public static final int CONFIG_SLOTS_PER_SALE = 2;
    public static final int CONFIG_SLOTS = SALE_SLOT_COUNT * CONFIG_SLOTS_PER_SALE;

    public static final int STOCK_SLOTS = 9;
    public static final int OUTPUT_SLOTS = 9;
    public static final int CASHBOX_SLOTS = 9;

    public static final int DEFAULT_STOCK_STACK_LIMIT = 1024;

    public static final int SIGN_PRESET_COUNT = 7;

    private int maxStockStackSize = DEFAULT_STOCK_STACK_LIMIT;

    private UUID ownerUuid = null;
    private String ownerName = "";

    private int signPreset = 0;

    public int getSignPreset() {
        return signPreset;
    }

    public void setSignPreset(int signPreset) {
        int max = Math.max(1, SIGN_PRESET_COUNT);
        this.signPreset = Math.floorMod(signPreset, max);

        setChanged();
        syncToClient();
    }

    public void cycleSignPreset(int amount) {
        setSignPreset(this.signPreset + amount);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        writeMachineData(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        readMachineData(tag, registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        readMachineData(packet.getTag(), registries);
    }

    public static int getSellConfigSlot(int saleIndex) {
        return saleIndex * CONFIG_SLOTS_PER_SALE;
    }

    public static int getPriceConfigSlot(int saleIndex) {
        return saleIndex * CONFIG_SLOTS_PER_SALE + 1;
    }

    private final ItemStackHandler configInventory = new ItemStackHandler(CONFIG_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            syncToClient();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 999;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 999;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (isVendingMachineStack(stack)) {
                return stack;
            }

    return super.insertItem(slot, stack, simulate);
}
    };

    private final ItemStackHandler stockInventory = new ItemStackHandler(STOCK_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return maxStockStackSize;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return maxStockStackSize;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (isVendingMachineStack(stack)) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }
    };

    private final ItemStackHandler outputInventory = new ItemStackHandler(OUTPUT_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return stack.getMaxStackSize();
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (isVendingMachineStack(stack)) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }
    };

    private final ItemStackHandler cashboxInventory = new ItemStackHandler(CASHBOX_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return maxStockStackSize;
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return maxStockStackSize;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (isVendingMachineStack(stack)) {
                return stack;
            }

            return super.insertItem(slot, stack, simulate);
        }
    };

    private boolean isVendingMachineStack(ItemStack stack) {
        return stack.is(ModBlocks.VENDING_MACHINE.get().asItem());
    }

    public VendingMachineBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.VENDING_MACHINE.get(), pos, blockState);
    }

    public ItemStackHandler getConfigInventory() {
        return configInventory;
    }

    public ItemStackHandler getStockInventory() {
        return stockInventory;
    }

    public ItemStackHandler getOutputInventory() {
        return outputInventory;
    }

    public ItemStackHandler getCashboxInventory() {
        return cashboxInventory;
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public String getOwnerName() {
        return ownerName == null ? "" : ownerName;
    }

    public void setOwner(Player player) {
        this.ownerUuid = player.getUUID();
        this.ownerName = player.getGameProfile().getName();
        setChanged();
    }

    public boolean canPlayerAdmin(Player player) {
        if (ownerUuid == null) {
            return true;
        }

        return player.getUUID().equals(ownerUuid) || player.hasPermissions(2);
    }

    public ItemStack getConfiguredSellStack(int saleIndex) {
        if (saleIndex < 0 || saleIndex >= SALE_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        return configInventory.getStackInSlot(getSellConfigSlot(saleIndex)).copy();
    }

    public ItemStack getConfiguredPriceStack(int saleIndex) {
        if (saleIndex < 0 || saleIndex >= SALE_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        return configInventory.getStackInSlot(getPriceConfigSlot(saleIndex)).copy();
    }

    public int getMaxStockStackSize() {
        return maxStockStackSize;
    }

    public void setMaxStockStackSize(int maxStockStackSize) {
        this.maxStockStackSize = Math.max(64, maxStockStackSize);
        setChanged();
    }

    public int getConfiguredSaleStockCount(int saleIndex) {
        ItemStack configuredSellStack = getConfiguredSellStack(saleIndex);

        if (configuredSellStack.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (int slot = 0; slot < stockInventory.getSlots(); slot++) {
            ItemStack stockStack = stockInventory.getStackInSlot(slot);

            if (ItemStack.isSameItemSameComponents(stockStack, configuredSellStack)) {
                count += stockStack.getCount();
            }
        }

        return count;
    }

    public boolean tryBuyConfiguredItem(Player player, int saleIndex) {
        ItemStack itemToSell = getConfiguredSellStack(saleIndex);
        ItemStack payment = getConfiguredPriceStack(saleIndex);

        if (itemToSell.isEmpty() || payment.isEmpty()) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("This sale slot is not configured."),
                    true
            );
            return false;
        }

        if (!hasItemInPlayerInventory(player, payment)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal(
                            "You need " + payment.getCount() + " x " + payment.getHoverName().getString() + "."
                    ),
                    true
            );
            return false;
        }

        if (!hasStockItem(itemToSell)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("This item is out of stock."),
                    true
            );
            return false;
        }

        if (!canInsertIntoOutput(itemToSell)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("The output tray is full."),
                    true
            );
            return false;
        }

        if (!canInsertIntoCashbox(payment)) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("The vending machine cashbox is full."),
                    true
            );
            return false;
        }

        removeItemFromPlayerInventory(player, payment);
        removeStockItem(itemToSell);
        insertIntoOutput(itemToSell.copy());
        insertIntoCashbox(payment.copy());

        setChanged();

        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "Purchased " + itemToSell.getCount() + " x " + itemToSell.getHoverName().getString() + "."
                ),
                true
        );

        return true;
    }

    private boolean hasStockItem(ItemStack required) {
        int remaining = required.getCount();

        for (int slot = 0; slot < stockInventory.getSlots(); slot++) {
            ItemStack stack = stockInventory.getStackInSlot(slot);

            if (ItemStack.isSameItemSameComponents(stack, required)) {
                remaining -= stack.getCount();

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private void removeStockItem(ItemStack required) {
        int remaining = required.getCount();

        for (int slot = 0; slot < stockInventory.getSlots(); slot++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack stack = stockInventory.getStackInSlot(slot);

            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toExtract = Math.min(remaining, stack.getCount());
                stockInventory.extractItem(slot, toExtract, false);
                remaining -= toExtract;
            }
        }
    }

    private boolean canInsertIntoOutput(ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();

        for (int slot = 0; slot < outputInventory.getSlots(); slot++) {
            remaining = outputInventory.insertItem(slot, remaining, true);

            if (remaining.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void insertIntoOutput(ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();

        for (int slot = 0; slot < outputInventory.getSlots(); slot++) {
            remaining = outputInventory.insertItem(slot, remaining, false);

            if (remaining.isEmpty()) {
                return;
            }
        }
    }

    private boolean canInsertIntoCashbox(ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();

        for (int slot = 0; slot < cashboxInventory.getSlots(); slot++) {
            remaining = cashboxInventory.insertItem(slot, remaining, true);

            if (remaining.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void insertIntoCashbox(ItemStack stackToInsert) {
        ItemStack remaining = stackToInsert.copy();

        for (int slot = 0; slot < cashboxInventory.getSlots(); slot++) {
            remaining = cashboxInventory.insertItem(slot, remaining, false);

            if (remaining.isEmpty()) {
                return;
            }
        }
    }

    private boolean hasItemInPlayerInventory(Player player, ItemStack required) {
        int remaining = required.getCount();

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (ItemStack.isSameItemSameComponents(stack, required)) {
                remaining -= stack.getCount();

                if (remaining <= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    private void removeItemFromPlayerInventory(Player player, ItemStack required) {
        int remaining = required.getCount();

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (remaining <= 0) {
                break;
            }

            ItemStack stack = player.getInventory().getItem(slot);

            if (ItemStack.isSameItemSameComponents(stack, required)) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;

                if (stack.isEmpty()) {
                    player.getInventory().setItem(slot, ItemStack.EMPTY);
                }
            }
        }

        player.getInventory().setChanged();
    }

    public void saveToItemStack(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag dataTag = new CompoundTag();

        writeMachineData(dataTag, registries);

        stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(dataTag));
    }

    public void loadFromItemStack(ItemStack stack, HolderLookup.Provider registries) {
        CustomData customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag dataTag = customData.copyTag();

        readMachineData(dataTag, registries);

        setChanged();
    }

    private void writeMachineData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("MaxStockStackSize", maxStockStackSize);
        tag.putInt("SignPreset", signPreset);
        tag.put("ConfigInventory", configInventory.serializeNBT(registries));
        tag.put("StockInventory", stockInventory.serializeNBT(registries));
        tag.put("OutputInventory", outputInventory.serializeNBT(registries));
        tag.put("CashboxInventory", cashboxInventory.serializeNBT(registries));

        if (ownerUuid != null) {
            tag.putUUID("OwnerUUID", ownerUuid);
            tag.putString("OwnerName", ownerName == null ? "" : ownerName);
        }
    }

    private void readMachineData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("MaxStockStackSize")) {
            maxStockStackSize = tag.getInt("MaxStockStackSize");
        }

        if (tag.contains("SignPreset")) {
            setSignPreset(tag.getInt("SignPreset"));
        }

        if (tag.contains("ConfigInventory")) {
            CompoundTag configTag = tag.getCompound("ConfigInventory");

            int savedSize = configTag.getInt("Size");

            if (savedSize >= CONFIG_SLOTS) {
                configInventory.deserializeNBT(registries, configTag);
            } else {
                ItemStackHandler oldConfig = new ItemStackHandler(savedSize);
                oldConfig.deserializeNBT(registries, configTag);

                if (savedSize > 0) {
                    configInventory.setStackInSlot(getSellConfigSlot(0), oldConfig.getStackInSlot(0).copy());
                }

                if (savedSize > 1) {
                    configInventory.setStackInSlot(getPriceConfigSlot(0), oldConfig.getStackInSlot(1).copy());
                }

                setChanged();
            }
        }

        if (tag.contains("StockInventory")) {
            stockInventory.deserializeNBT(registries, tag.getCompound("StockInventory"));
        }

        if (tag.contains("OutputInventory")) {
            outputInventory.deserializeNBT(registries, tag.getCompound("OutputInventory"));
        }

        if (tag.contains("CashboxInventory")) {
            cashboxInventory.deserializeNBT(registries, tag.getCompound("CashboxInventory"));
        }

        if (tag.hasUUID("OwnerUUID")) {
            ownerUuid = tag.getUUID("OwnerUUID");
            ownerName = tag.getString("OwnerName");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        writeMachineData(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        readMachineData(tag, registries);
    }
}