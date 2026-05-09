package com.robby.vendingmachine.client;

import com.robby.vendingmachine.menu.VendingMachineConfigMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VendingMachineConfigScreen extends AbstractContainerScreen<VendingMachineConfigMenu> {
    public VendingMachineConfigScreen(VendingMachineConfigMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 384;

        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 278;
    }

    @Override
    protected void init() {
        super.init();

        int startY = this.topPos + 30;

        for (int saleIndex = 0; saleIndex < VendingMachineConfigMenu.SALE_SLOT_COUNT; saleIndex++) {
            int y = startY + saleIndex * 26;

            addQuantityButtons(saleIndex, false, this.leftPos + 32, y);
            addQuantityButtons(saleIndex, true, this.leftPos + 116, y);
        }

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Close"),
                                button -> this.onClose()
                        )
                        .bounds(this.leftPos + 118, this.topPos + 258, 50, 20)
                        .build()
        );
    }

    private void sendMenuButtonClick(int buttonId) {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.gameMode == null) {
            return;
        }

        this.menu.clickMenuButton(this.minecraft.player, buttonId);
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
    }

    private void addQuantityButtons(int saleIndex, boolean priceSlot, int x, int y) {
        this.addRenderableWidget(
                Button.builder(
                                Component.literal("+"),
                                button -> sendMenuButtonClick(getButtonId(saleIndex, priceSlot, true))
                        )
                        .bounds(x, y, 20, 12)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("-"),
                                button -> sendMenuButtonClick(getButtonId(saleIndex, priceSlot, false))
                        )
                        .bounds(x, y + 13, 20, 12)
                        .build()
        );
    }

    private int getButtonId(int saleIndex, boolean priceSlot, boolean plus) {
        int action;

        if (Screen.hasControlDown() && Screen.hasShiftDown()) {
            action = plus ? 4 : 5;
        } else if (Screen.hasShiftDown()) {
            action = plus ? 2 : 3;
        } else {
            action = plus ? 0 : 1;
        }

        int priceOffset = priceSlot ? 6 : 0;
        return saleIndex * 12 + priceOffset + action;
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

        // Config section.
        guiGraphics.fill(x + 4, y + 24, x + this.imageWidth - 4, y + 282, 0xFF1F2730);

        // Player inventory section.
        guiGraphics.fill(x + 4, y + 286, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);

        drawConfigSlots(guiGraphics, x + 8, y + 30);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, Component.literal("Sell"), 8, 18, 0xA0C8FF, false);
        guiGraphics.drawString(this.font, Component.literal("Price"), 92, 18, 0xA0C8FF, false);

        for (int saleIndex = 0; saleIndex < VendingMachineConfigMenu.SALE_SLOT_COUNT; saleIndex++) {
            int y = 34 + saleIndex * 26;

            guiGraphics.drawString(
                    this.font,
                    Component.literal(String.valueOf(saleIndex + 1)),
                    158,
                    y,
                    0xFFFFFF,
                    false
            );
        }

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    private void drawConfigSlots(GuiGraphics guiGraphics, int x, int y) {
        for (int saleIndex = 0; saleIndex < VendingMachineConfigMenu.SALE_SLOT_COUNT; saleIndex++) {
            int rowY = y + saleIndex * 26;

            drawSlot(guiGraphics, x, rowY);
            drawSlot(guiGraphics, x + 84, rowY);
        }
    }

    private void drawPlayerInventorySlots(GuiGraphics guiGraphics, int x, int y) {
        for (int row = 0; row < 3; row++) {
            drawSlotRow(guiGraphics, x, y + row * 18, 9);
        }

        drawSlotRow(guiGraphics, x, y + 58, 9);
    }

    private void drawSlotRow(GuiGraphics guiGraphics, int x, int y, int slots) {
        for (int i = 0; i < slots; i++) {
            drawSlot(guiGraphics, x + i * 18, y);
        }
    }

    private void drawSlot(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFF111111);
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
        guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF3A3A3A);
    }
}