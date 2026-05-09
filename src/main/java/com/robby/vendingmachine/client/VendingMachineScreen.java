package com.robby.vendingmachine.client;

import com.robby.vendingmachine.menu.VendingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VendingMachineScreen extends AbstractContainerScreen<VendingMachineMenu> {
    public VendingMachineScreen(VendingMachineMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 332;

        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 228;
    }

    @Override
    protected void init() {
        super.init();

        int startX = this.leftPos + 8;
        int startY = this.topPos + 34;

        for (int saleIndex = 0; saleIndex < VendingMachineMenu.SALE_SLOT_COUNT; saleIndex++) {
            int row = saleIndex / 3;
            int col = saleIndex % 3;

            int x = startX + col * 54;
            int y = startY + row * 44;

            final int buttonId = saleIndex;

            this.addRenderableWidget(
                    Button.builder(
                                    Component.literal("Buy"),
                                    button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId)
                            )
                            .bounds(x, y + 18, 46, 14)
                            .build()
            );
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2F2F2F);
        guiGraphics.fill(x + 4, y + 4, x + this.imageWidth - 4, y + 18, 0xFF3F3F3F);

        // Shop section.
        guiGraphics.fill(x + 4, y + 24, x + this.imageWidth - 4, y + 174, 0xFF252525);

        // Output tray section.
        guiGraphics.fill(x + 4, y + 182, x + this.imageWidth - 4, y + 214, 0xFF252525);

        // Player inventory section.
        guiGraphics.fill(x + 4, y + 236, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);

        drawSaleDisplaySlots(guiGraphics, x + 8, y + 34);
        drawSlotRow(guiGraphics, x + 8, y + 186, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 240);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, Component.literal("Items for Sale"), 8, 24, 0xFFFFFF, false);

        for (int saleIndex = 0; saleIndex < VendingMachineMenu.SALE_SLOT_COUNT; saleIndex++) {
            int row = saleIndex / 3;
            int col = saleIndex % 3;

            int x = 8 + col * 54;
            int y = 34 + row * 44;

            guiGraphics.drawString(
                    this.font,
                    Component.literal("S:" + this.menu.getConfiguredSaleStockCount(saleIndex)),
                    x,
                    y + 33,
                    0xA0FFA0,
                    false
            );
        }

        guiGraphics.drawString(this.font, Component.literal("Purchased Items"), 8, 216, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    private void drawSaleDisplaySlots(GuiGraphics guiGraphics, int x, int y) {
        for (int saleIndex = 0; saleIndex < VendingMachineMenu.SALE_SLOT_COUNT; saleIndex++) {
            int row = saleIndex / 3;
            int col = saleIndex % 3;

            int slotX = x + col * 54;
            int slotY = y + row * 44;

            drawSlot(guiGraphics, slotX, slotY);
            drawSlot(guiGraphics, slotX + 24, slotY);
        }
    }

    private void drawSlotRow(GuiGraphics guiGraphics, int x, int y, int slots) {
        for (int i = 0; i < slots; i++) {
            drawSlot(guiGraphics, x + i * 18, y);
        }
    }

    private void drawPlayerInventorySlots(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            drawSlotRow(guiGraphics, x, y + row * 18, 9);
        }

        drawSlotRow(guiGraphics, x, y + 58, 9);
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF111111);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF3A3A3A);
    }
}