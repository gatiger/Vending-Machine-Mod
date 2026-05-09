package com.robby.vendingmachine.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class VendingMachineBlockItem extends BlockItem {
    public VendingMachineBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltipComponents,
            TooltipFlag tooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);

        if (blockEntityData == null) {
            tooltipComponents.add(
                    Component.literal("New vending machine")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        CompoundTag tag = blockEntityData.copyTag();

        tooltipComponents.add(
                Component.literal("Contains saved vending machine data")
                        .withStyle(ChatFormatting.GOLD)
        );

        if (tag.contains("OwnerName")) {
            String ownerName = tag.getString("OwnerName");

            if (!ownerName.isBlank()) {
                tooltipComponents.add(
                        Component.literal("Owner: " + ownerName)
                                .withStyle(ChatFormatting.YELLOW)
                );
            }
        }

        if (tag.contains("StockInventory") || tag.contains("CashboxInventory") || tag.contains("ConfigInventory")) {
            tooltipComponents.add(
                    Component.literal("Keeps config, inventory, and cashbox")
                            .withStyle(ChatFormatting.DARK_GRAY)
            );
        }
    }
}