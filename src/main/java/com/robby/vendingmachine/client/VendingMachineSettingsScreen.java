package com.robby.vendingmachine.client;

import com.robby.vendingmachine.menu.VendingMachineSettingsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class VendingMachineSettingsScreen extends AbstractContainerScreen<VendingMachineSettingsMenu> {
    private static final String[] SIGN_NAMES = {
            "Ingots",
            "Food",
            "Tools",
            "Ores",
            "Blocks",
            "Magic",
            "General"
    };

    public VendingMachineSettingsScreen(VendingMachineSettingsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 96;

        this.titleLabelX = 8;
        this.titleLabelY = 6;

        this.inventoryLabelX = 8;
        this.inventoryLabelY = 1000; // hidden/offscreen because this screen has no inventory slots
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("<"),
                                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0)
                        )
                        .bounds(this.leftPos + 18, this.topPos + 42, 24, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal(">"),
                                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1)
                        )
                        .bounds(this.leftPos + 134, this.topPos + 42, 24, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("Back"),
                                button -> this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 999)
                        )
                        .bounds(this.leftPos + 68, this.topPos + 68, 40, 20)
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

        guiGraphics.fill(x + 8, y + 26, x + this.imageWidth - 8, y + 64, 0xFF1F2730);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, Component.literal("Display Sign"), 12, 28, 0xA0C8FF, false);

        String signName = getSignName(this.menu.getSignPreset());
        int textWidth = this.font.width(signName);

        guiGraphics.drawString(
                this.font,
                Component.literal(signName),
                (this.imageWidth - textWidth) / 2,
                48,
                0xFFFFFF,
                false
        );
    }

    private String getSignName(int index) {
        if (index < 0 || index >= SIGN_NAMES.length) {
            return SIGN_NAMES[0];
        }

        return SIGN_NAMES[index];
    }
}