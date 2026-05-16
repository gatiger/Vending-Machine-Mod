package com.robby.vendingmachine.client;

import com.robby.vendingmachine.menu.VendingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VendingMachineScreen extends AbstractContainerScreen<VendingMachineMenu> {
    private static final int COLOR_BG_DARK = 0xFF171A1F;
    private static final int COLOR_PANEL = 0xFF242A32;
    private static final int COLOR_PANEL_2 = 0xFF1E242C;
    private static final int COLOR_HEADER = 0xFF354152;
    private static final int COLOR_BORDER_DARK = 0xFF0B0D10;
    private static final int COLOR_BORDER_LIGHT = 0xFF536070;
    private static final int COLOR_ACCENT_DARK = 0xFF1B5D78;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xFFAAB4C0;
    private static final int COLOR_STOCK = 0xFF1E3327;
    private static final int COLOR_OUTPUT = 0xFF332323;
    private static final int COLOR_DISPLAY = 0xFF202C3A;

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

        int startX = this.leftPos + 14;
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

        drawBase(guiGraphics, x, y);

        // Shop section.
        drawPanel(guiGraphics, x + 4, y + 24, this.imageWidth - 8, 150, COLOR_DISPLAY);

        // Output tray section.
        drawPanel(guiGraphics, x + 4, y + 182, this.imageWidth - 8, 32, COLOR_OUTPUT);

        // Player inventory section.
        drawPanel(guiGraphics, x + 4, y + 236, this.imageWidth - 8, this.imageHeight - 240, COLOR_PANEL_2);

        drawSaleDisplaySlots(guiGraphics, x + 14, y + 34);
        drawSlotRow(guiGraphics, x + 8, y + 186, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 240);
    }

    private void drawBase(GuiGraphics guiGraphics, int x, int y) {
        // Outer frame.
        guiGraphics.fill(x - 2, y - 2, x + this.imageWidth + 2, y + this.imageHeight + 2, COLOR_BORDER_DARK);
        guiGraphics.fill(x - 1, y - 1, x + this.imageWidth + 1, y + this.imageHeight + 1, COLOR_BORDER_LIGHT);

        // Main background.
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, COLOR_BG_DARK);

        // Header.
        guiGraphics.fill(x + 4, y + 4, x + this.imageWidth - 4, y + 20, COLOR_HEADER);
        guiGraphics.fill(x + 4, y + 20, x + this.imageWidth - 4, y + 21, COLOR_ACCENT_DARK);

        // Main content panel.
        guiGraphics.fill(x + 4, y + 24, x + this.imageWidth - 4, y + this.imageHeight - 4, COLOR_PANEL);
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int fillColor) {
        drawRoundedPanel(guiGraphics, x - 1, y - 1, width + 2, height + 2, fillColor);
    }

    private void drawRoundedPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int fillColor) {
        int border = COLOR_BORDER_DARK;
        int highlight = COLOR_BORDER_LIGHT;

        // Outer dark rounded border.
        guiGraphics.fill(x + 2, y, x + width - 2, y + 1, border);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, border);
        guiGraphics.fill(x, y + 2, x + width, y + height - 2, border);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, border);
        guiGraphics.fill(x + 2, y + height - 1, x + width - 2, y + height, border);

        // Main fill with stepped corners.
        guiGraphics.fill(x + 3, y + 1, x + width - 3, y + height - 1, fillColor);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
        guiGraphics.fill(x + 1, y + 3, x + width - 1, y + height - 3, fillColor);

        // Subtle top/left highlight.
        guiGraphics.fill(x + 3, y + 1, x + width - 3, y + 2, highlight);
        guiGraphics.fill(x + 1, y + 3, x + 2, y + height - 3, highlight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Center title inside the header bar.
        int titleWidth = this.font.width(this.title);
        int titleX = (this.imageWidth - titleWidth) / 2;
        int titleY = 8;

        guiGraphics.drawString(this.font, this.title, titleX, titleY, COLOR_TEXT, false);

        // Center "Items for Sale" over the shop area.
        Component itemsForSale = Component.literal("Items for Sale");
        int itemsWidth = this.font.width(itemsForSale);
        int itemsX = (this.imageWidth - itemsWidth) / 2;

        guiGraphics.drawString(this.font, itemsForSale, itemsX, 24, 0xFFA0C8FF, false);

        for (int saleIndex = 0; saleIndex < VendingMachineMenu.SALE_SLOT_COUNT; saleIndex++) {
            int row = saleIndex / 3;
            int col = saleIndex % 3;

            // Match the centered sale grid position from init().
            int x = 14 + col * 54;
            int y = 34 + row * 44;

            guiGraphics.drawString(
                    this.font,
                    Component.literal("S:" + this.menu.getConfiguredSaleStockCount(saleIndex)),
                    x,
                    y + 34,
                    0xFFA0FFA0,
                    false
            );
        }

        // Move this up into the reddish output tray area.
        guiGraphics.drawString(this.font, Component.literal("Purchased Items"), 8, 204, 0xFFFFB0B0, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, COLOR_TEXT_MUTED, false);
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
        // Outer dark border.
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, COLOR_BORDER_DARK);

        // Slot bevel.
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF5D6670);
        guiGraphics.fill(x + 1, y + 1, x + 16, y + 16, 0xFF15191E);
        guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF2A3038);

        // Inner subtle highlight.
        guiGraphics.fill(x + 2, y + 2, x + 14, y + 3, 0xFF3A424C);
    }
}