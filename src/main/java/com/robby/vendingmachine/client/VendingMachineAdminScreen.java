package com.robby.vendingmachine.client;

import com.robby.vendingmachine.menu.VendingMachineAdminMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class VendingMachineAdminScreen extends AbstractContainerScreen<VendingMachineAdminMenu> {
    private static final int TAB_WIDTH = 28;
    private static final int TAB_HEIGHT = 24;

    private static final String[] SIGN_NAMES = {
            "Ingots",
            "Food",
            "Tools",
            "Ores",
            "Blocks",
            "Magic",
            "General"
    };

    private AdminTab activeTab = AdminTab.STOCK;

    private Button signPreviousButton;
    private Button signNextButton;
    private final List<Button> salesQuantityButtons = new ArrayList<>();

    private enum AdminTab {
        STOCK("Stock", "Stock"),
        SALES("Sales", "Sales"),
        CASHBOX("Cash", "Cash"),
        DISPLAY("Display", "Display");

        private final String buttonText;
        private final String title;

        AdminTab(String buttonText, String title) {
            this.buttonText = buttonText;
            this.title = title;
        }
    }

    public VendingMachineAdminScreen(VendingMachineAdminMenu menu, Inventory playerInventory, Component title) {
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

        this.menu.setActiveTabIndex(this.activeTab.ordinal());

        this.signPreviousButton = null;
        this.signNextButton = null;
        this.salesQuantityButtons.clear();

        addTabButtons();
        addDisplayButtons();
        addSalesQuantityButtons();

        rebuildTabButtons();
    }

    private void addTabButtons() {
        int tabX = this.leftPos + this.imageWidth - 1;
        int tabY = this.topPos + 24;

        AdminTab[] tabs = AdminTab.values();

        for (int i = 0; i < tabs.length; i++) {
            AdminTab tab = tabs[i];

            this.addRenderableWidget(
                    Button.builder(
                                    Component.literal(tab.buttonText),
                                    button -> {
                                        this.activeTab = tab;
                                        this.menu.setActiveTabIndex(tab.ordinal());

                                        if (this.minecraft != null && this.minecraft.gameMode != null) {
                                            this.minecraft.gameMode.handleInventoryButtonClick(
                                                    this.menu.containerId,
                                                    VendingMachineAdminMenu.TAB_BUTTON_OFFSET + tab.ordinal()
                                            );
                                        }

                                        rebuildTabButtons();
                                    }
                            )
                            .bounds(tabX, tabY + i * (TAB_HEIGHT + 2), TAB_WIDTH, TAB_HEIGHT)
                            .build()
            );
        }
    }

    private void addDisplayButtons() {
        this.signPreviousButton = Button.builder(
                        Component.literal("<"),
                        button -> sendMenuButtonClick(VendingMachineAdminMenu.SIGN_PREVIOUS_BUTTON_ID)
                )
                .bounds(this.leftPos + 24, this.topPos + 72, 24, 20)
                .build();

        this.signNextButton = Button.builder(
                        Component.literal(">"),
                        button -> sendMenuButtonClick(VendingMachineAdminMenu.SIGN_NEXT_BUTTON_ID)
                )
                .bounds(this.leftPos + 128, this.topPos + 72, 24, 20)
                .build();

        this.addRenderableWidget(this.signPreviousButton);
        this.addRenderableWidget(this.signNextButton);
    }

    private void addSalesQuantityButtons() {
        int startY = this.topPos + 30;

        for (int saleIndex = 0; saleIndex < VendingMachineAdminMenu.SALE_SLOT_COUNT; saleIndex++) {
            int y = startY + saleIndex * 26;

            addQuantityButtons(saleIndex, false, this.leftPos + 32, y);
            addQuantityButtons(saleIndex, true, this.leftPos + 116, y);
        }
    }

    private void addQuantityButtons(int saleIndex, boolean priceSlot, int x, int y) {
        Button plusButton = Button.builder(
                        Component.literal("+"),
                        button -> sendMenuButtonClick(getSalesButtonId(saleIndex, priceSlot, true))
                )
                .bounds(x, y, 20, 12)
                .build();

        Button minusButton = Button.builder(
                        Component.literal("-"),
                        button -> sendMenuButtonClick(getSalesButtonId(saleIndex, priceSlot, false))
                )
                .bounds(x, y + 13, 20, 12)
                .build();

        this.salesQuantityButtons.add(plusButton);
        this.salesQuantityButtons.add(minusButton);

        this.addRenderableWidget(plusButton);
        this.addRenderableWidget(minusButton);
    }

    private int getSalesButtonId(int saleIndex, boolean priceSlot, boolean plus) {
        int action;

        if (Screen.hasControlDown() && Screen.hasShiftDown()) {
            action = plus ? 4 : 5;
        } else if (Screen.hasShiftDown()) {
            action = plus ? 2 : 3;
        } else {
            action = plus ? 0 : 1;
        }

        int priceOffset = priceSlot ? 6 : 0;
        return VendingMachineAdminMenu.SALES_BUTTON_OFFSET + saleIndex * 12 + priceOffset + action;
    }

    private void sendMenuButtonClick(int buttonId) {
        if (this.minecraft == null || this.minecraft.player == null || this.minecraft.gameMode == null) {
            return;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, buttonId);
    }

    private void rebuildTabButtons() {
        boolean displayVisible = this.activeTab == AdminTab.DISPLAY;
        boolean salesVisible = this.activeTab == AdminTab.SALES;

        this.signPreviousButton.visible = displayVisible;
        this.signPreviousButton.active = displayVisible;

        this.signNextButton.visible = displayVisible;
        this.signNextButton.active = displayVisible;

        for (Button button : this.salesQuantityButtons) {
            button.visible = salesVisible;
            button.active = salesVisible;
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
        drawTabs(guiGraphics, x, y);

        switch (this.activeTab) {
            case STOCK -> drawStockTab(guiGraphics, x, y);
            case SALES -> drawSalesTab(guiGraphics, x, y);
            case CASHBOX -> drawCashboxTab(guiGraphics, x, y);
            case DISPLAY -> drawDisplayTab(guiGraphics, x, y);
        }
    }

    private void drawBase(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFF2F2F2F);
        guiGraphics.fill(x + 4, y + 4, x + this.imageWidth - 4, y + 18, 0xFF3F3F3F);
        guiGraphics.fill(x + 4, y + 22, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);
    }

    private void drawTabs(GuiGraphics guiGraphics, int x, int y) {
        int tabX = x + this.imageWidth - 1;
        int tabY = y + 24;

        AdminTab[] tabs = AdminTab.values();

        for (int i = 0; i < tabs.length; i++) {
            AdminTab tab = tabs[i];
            int top = tabY + i * (TAB_HEIGHT + 2);
            boolean selected = tab == this.activeTab;

            int fill = selected ? 0xFF3F4B5A : 0xFF2A2F36;
            int border = selected ? 0xFF78C8FF : 0xFF111111;

            guiGraphics.fill(tabX - 1, top - 1, tabX + TAB_WIDTH + 1, top + TAB_HEIGHT + 1, border);
            guiGraphics.fill(tabX, top, tabX + TAB_WIDTH, top + TAB_HEIGHT, fill);
        }
    }

    private void drawStockTab(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 8, y + 28, x + this.imageWidth - 8, y + 104, 0xFF1F2A1F);
        guiGraphics.fill(x + 8, y + 112, x + this.imageWidth - 8, y + 160, 0xFF2A1F1F);
        guiGraphics.fill(x + 4, y + 286, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);

        drawSlotRow(guiGraphics, x + 8, y + 78, 9);
        drawSlotRow(guiGraphics, x + 8, y + 136, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    private void drawSalesTab(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 4, y + 24, x + this.imageWidth - 4, y + 282, 0xFF1F2730);
        guiGraphics.fill(x + 4, y + 286, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);

        drawConfigSlots(guiGraphics, x + 8, y + 30);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    private void drawCashboxTab(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 8, y + 28, x + this.imageWidth - 8, y + 104, 0xFF2A281F);
        guiGraphics.fill(x + 4, y + 286, x + this.imageWidth - 4, y + this.imageHeight - 4, 0xFF252525);

        drawSlotRow(guiGraphics, x + 8, y + 78, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    private void drawDisplayTab(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x + 8, y + 28, x + this.imageWidth - 8, y + 130, 0xFF1F2730);
        guiGraphics.fill(x + 16, y + 52, x + this.imageWidth - 16, y + 104, 0xFF18202A);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        switch (this.activeTab) {
            case STOCK -> renderStockLabels(guiGraphics);
            case SALES -> renderSalesLabels(guiGraphics);
            case CASHBOX -> renderCashboxLabels(guiGraphics);
            case DISPLAY -> renderDisplayLabels(guiGraphics);
        }
    }

    private void renderStockLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, Component.literal("Stock Inventory"), 12, 32, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Items available for sale"), 12, 42, 0xA0FFA0, false);

        guiGraphics.drawString(this.font, Component.literal("Output Tray"), 12, 112, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Purchased items waiting for pickup"), 12, 122, 0xFFB0B0, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    private void renderSalesLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFFFFF, false);

        guiGraphics.drawString(this.font, Component.literal("Sell"), 8, 18, 0xA0C8FF, false);
        guiGraphics.drawString(this.font, Component.literal("Price"), 92, 18, 0xA0C8FF, false);

        for (int saleIndex = 0; saleIndex < VendingMachineAdminMenu.SALE_SLOT_COUNT; saleIndex++) {
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

    private void renderCashboxLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, Component.literal("Cashbox"), 12, 32, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Payments collected from customers"), 12, 44, 0xFFE0A0, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    private void renderDisplayLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, Component.literal("Display Settings"), 12, 32, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Choose the vending machine sign."), 12, 44, 0xA0C8FF, false);

        String signName = getSignName(this.menu.getSignPreset());
        int signTextWidth = this.font.width(signName);

        guiGraphics.drawString(this.font, Component.literal("Sign Preset"), 20, 60, 0xFFE0A0, false);

        guiGraphics.drawString(
                this.font,
                Component.literal(signName),
                (this.imageWidth - signTextWidth) / 2,
                78,
                0xFFFFFF,
                false
        );

        guiGraphics.drawString(
                this.font,
                Component.literal("Changes update the machine display immediately."),
                16,
                110,
                0xAAAAAA,
                false
        );
    }

    private String getSignName(int index) {
        if (index < 0 || index >= SIGN_NAMES.length) {
            return SIGN_NAMES[0];
        }

        return SIGN_NAMES[index];
    }

    private void drawConfigSlots(GuiGraphics guiGraphics, int x, int y) {
        for (int saleIndex = 0; saleIndex < VendingMachineAdminMenu.SALE_SLOT_COUNT; saleIndex++) {
            int rowY = y + saleIndex * 26;

            drawSlot(guiGraphics, x, rowY);
            drawSlot(guiGraphics, x + 84, rowY);
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