package com.robby.vendingmachine.client;

import com.robby.vendingmachine.menu.VendingMachineAdminMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VendingMachineAdminScreen extends AbstractContainerScreen<VendingMachineAdminMenu> {
    private Button configureButton;

    public VendingMachineAdminScreen(VendingMachineAdminMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 336;

        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 228;
    }

    @Override
    protected void init() {
        super.init();

        this.configureButton = Button.builder(
                        Component.literal("Configure Prices"),
                        button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0)
                )
                .bounds(this.leftPos + 8, this.topPos + 24, 160, 20)
                .build();

        this.addRenderableWidget(this.configureButton);

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("⚙"),
                                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1)
                        )
                        .bounds(this.leftPos + 152, this.topPos + 4, 16, 16)
                        .build()
        );
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

        // Config button section.
        guiGraphics.fill(x + 4, y + 22, x + this.imageWidth - 4, y + 48, 0xFF1F2730);

        // Stock section.
        guiGraphics.fill(x + 4, y + 56, x + this.imageWidth - 4, y + 100, 0xFF1F2A1F);

        // Output tray section.
        guiGraphics.fill(x + 4, y + 110, x + this.imageWidth - 4, y + 158, 0xFF2A1F1F);

        // Cashbox section.
        guiGraphics.fill(x + 4, y + 168, x + this.imageWidth - 4, y + 216, 0xFF2A281F);

        // Player inventory section.
        guiGraphics.fill(x + 4, y + 236, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);

        drawSlotRow(guiGraphics, x + 8, y + 78, 9);
        drawSlotRow(guiGraphics, x + 8, y + 136, 9);
        drawSlotRow(guiGraphics, x + 8, y + 194, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 240);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, Component.literal("Stock Inventory"), 8, 56, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Items available for sale"), 8, 66, 0xA0FFA0, false);

        guiGraphics.drawString(this.font, Component.literal("Output Tray"), 8, 110, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Purchased items"), 8, 120, 0xFFB0B0, false);

        guiGraphics.drawString(this.font, Component.literal("Cashbox"), 8, 168, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Payments collected"), 8, 178, 0xFFE0A0, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
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