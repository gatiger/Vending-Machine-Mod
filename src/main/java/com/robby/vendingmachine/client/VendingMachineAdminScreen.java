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
    private static final int TAB_WIDTH = 38;
    private static final int TAB_HEIGHT = 26;
    private static final int TAB_GAP = 2;
    private static final int TAB_HIDDEN_AMOUNT_ACTIVE = 12;
    private static final int TAB_HIDDEN_AMOUNT_INACTIVE = 18;
    private static final int TAB_X_SHIFT = 4;

    private static final int COLOR_BG_DARK = 0xFF171A1F;
    private static final int COLOR_PANEL = 0xFF242A32;
    private static final int COLOR_PANEL_2 = 0xFF1E242C;
    private static final int COLOR_HEADER = 0xFF354152;
    private static final int COLOR_BORDER_DARK = 0xFF0B0D10;
    private static final int COLOR_BORDER_LIGHT = 0xFF536070;
    private static final int COLOR_ACCENT = 0xFF46C8FF;
    private static final int COLOR_ACCENT_DARK = 0xFF1B5D78;
    private static final int COLOR_TEXT = 0xFFFFFFFF;
    private static final int COLOR_TEXT_MUTED = 0xFFAAB4C0;
    private static final int COLOR_STOCK = 0xFF1E3327;
    private static final int COLOR_OUTPUT = 0xFF332323;
    private static final int COLOR_CASH = 0xFF332F20;
    private static final int COLOR_SALES = 0xFF1D2B3A;
    private static final int COLOR_DISPLAY = 0xFF202C3A;

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
        STOCK("Stk", "Stock"),
        SALES("Sale", "Sales"),
        CASHBOX("Cash", "Cashbox"),
        DISPLAY("Disp", "Display");

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

        addDisplayButtons();
        addSalesQuantityButtons();

        rebuildTabButtons();
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

        // Draw inactive tabs first so the main GUI covers their left edges.
        drawInactiveTabs(guiGraphics, x, y);

        drawBase(guiGraphics, x, y);

        switch (this.activeTab) {
            case STOCK -> drawStockTab(guiGraphics, x, y);
            case SALES -> drawSalesTab(guiGraphics, x, y);
            case CASHBOX -> drawCashboxTab(guiGraphics, x, y);
            case DISPLAY -> drawDisplayTab(guiGraphics, x, y);
        }

        // Draw the active tab last so it appears attached to the GUI face.
        drawActiveTab(guiGraphics, x, y);
    }

    private record TabBounds(int left, int top, int right, int bottom) {
    }

    private TabBounds getTabBounds(int x, int y, int index, boolean selected) {
        int guiRight = x + this.imageWidth;
        int tabTop = y + 30 + index * (TAB_HEIGHT + TAB_GAP);

        int tabLeft;
        if (selected) {
            // Active tab begins at the GUI edge so it looks attached.
            tabLeft = guiRight - 1;
        } else {
            // Inactive tabs stay tucked behind the GUI.
            tabLeft = guiRight - 18;
        }

        int tabRight = tabLeft + TAB_WIDTH;
        int tabBottom = tabTop + TAB_HEIGHT;

        return new TabBounds(tabLeft, tabTop, tabRight, tabBottom);
    }

    private void drawBase(GuiGraphics guiGraphics, int x, int y) {
        // Outer frame
        guiGraphics.fill(x - 2, y - 2, x + this.imageWidth + 2, y + this.imageHeight + 2, COLOR_BORDER_DARK);
        guiGraphics.fill(x - 1, y - 1, x + this.imageWidth + 1, y + this.imageHeight + 1, COLOR_BORDER_LIGHT);

        // Main background
        guiGraphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, COLOR_BG_DARK);

        // Header
        guiGraphics.fill(x + 4, y + 4, x + this.imageWidth - 4, y + 20, COLOR_HEADER);
        guiGraphics.fill(x + 4, y + 20, x + this.imageWidth - 4, y + 21, COLOR_ACCENT_DARK);

        // Main content panel
        guiGraphics.fill(x + 4, y + 24, x + this.imageWidth - 4, y + this.imageHeight - 4, COLOR_PANEL);
    }

    private void drawInactiveTabs(GuiGraphics guiGraphics, int x, int y) {
        AdminTab[] tabs = AdminTab.values();

        for (int i = 0; i < tabs.length; i++) {
            AdminTab tab = tabs[i];

            if (tab != this.activeTab) {
                drawSingleTab(guiGraphics, x, y, tab, i, false);
            }
        }
    }

    private void drawActiveTab(GuiGraphics guiGraphics, int x, int y) {
        AdminTab[] tabs = AdminTab.values();

        for (int i = 0; i < tabs.length; i++) {
            AdminTab tab = tabs[i];

            if (tab == this.activeTab) {
                drawSingleTab(guiGraphics, x, y, tab, i, true);
                return;
            }
        }
    }

    private void drawSingleTab(GuiGraphics guiGraphics, int x, int y, AdminTab tab, int index, boolean selected) {
        TabBounds bounds = getTabBounds(x, y, index, selected);

        int guiRight = x + this.imageWidth;

        int borderColor = selected ? COLOR_ACCENT : COLOR_BORDER_LIGHT;
        int fillColor = selected ? COLOR_HEADER : COLOR_PANEL_2;
        int lightEdge = selected ? COLOR_ACCENT : COLOR_BORDER_LIGHT;
        int darkEdge = COLOR_BORDER_DARK;
        int shadowColor = 0x66000000;

        // Shadow
        guiGraphics.fill(bounds.left + 2, bounds.top + 2, bounds.right + 2, bounds.bottom + 2, shadowColor);

        if (selected) {
            // Erase the normal right border segment where the active tab attaches,
            // so the active tab becomes part of the outer frame.
            guiGraphics.fill(guiRight - 2, bounds.top - 1, guiRight + 2, bounds.bottom + 1, COLOR_PANEL);
        }

        drawRoundedTab(
                guiGraphics,
                bounds.left,
                bounds.top,
                bounds.right - bounds.left,
                bounds.bottom - bounds.top,
                fillColor,
                borderColor
        );

        if (selected) {
            // Small bridge so the active tab visually merges with the GUI body.
            guiGraphics.fill(guiRight - 1, bounds.top + 1, bounds.left + 2, bounds.bottom - 1, COLOR_HEADER);

            // Re-draw the top and bottom outline where the GUI border wraps into the tab.
            guiGraphics.fill(guiRight - 1, bounds.top, bounds.left + 1, bounds.top + 1, borderColor);
            guiGraphics.fill(guiRight - 1, bounds.bottom - 1, bounds.left + 1, bounds.bottom, borderColor);
        }

        int textWidth = this.font.width(tab.buttonText);
        int textX = bounds.left + (TAB_WIDTH - textWidth) / 2;
        int textY = bounds.top + (TAB_HEIGHT - 8) / 2;

        guiGraphics.drawString(
                this.font,
                Component.literal(tab.buttonText),
                textX,
                textY,
                selected ? COLOR_TEXT : COLOR_TEXT_MUTED,
                false
        );
    }

    private void drawStockTab(GuiGraphics guiGraphics, int x, int y) {
        drawPanel(guiGraphics, x + 8, y + 28, this.imageWidth - 16, 76, COLOR_STOCK);
        drawPanel(guiGraphics, x + 8, y + 112, this.imageWidth - 16, 48, COLOR_OUTPUT);
        drawPanel(guiGraphics, x + 4, y + 286, this.imageWidth - 8, this.imageHeight - 290, COLOR_PANEL_2);

        drawSlotRow(guiGraphics, x + 8, y + 78, 9);
        drawSlotRow(guiGraphics, x + 8, y + 136, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    private void drawSalesTab(GuiGraphics guiGraphics, int x, int y) {
        drawPanel(guiGraphics, x + 4, y + 24, this.imageWidth - 8, 258, COLOR_SALES);
        drawPanel(guiGraphics, x + 4, y + 286, this.imageWidth - 8, this.imageHeight - 290, COLOR_PANEL_2);

        drawConfigSlots(guiGraphics, x + 8, y + 30);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    private void drawCashboxTab(GuiGraphics guiGraphics, int x, int y) {
        drawPanel(guiGraphics, x + 8, y + 28, this.imageWidth - 16, 76, COLOR_CASH);
        drawPanel(guiGraphics, x + 4, y + 286, this.imageWidth - 8, this.imageHeight - 290, COLOR_PANEL_2);

        drawSlotRow(guiGraphics, x + 8, y + 78, 9);
        drawPlayerInventorySlots(guiGraphics, x + 8, y + 290);
    }

    private void drawDisplayTab(GuiGraphics guiGraphics, int x, int y) {
        drawPanel(guiGraphics, x + 8, y + 28, this.imageWidth - 16, 102, COLOR_DISPLAY);
        drawPanel(guiGraphics, x + 16, y + 52, this.imageWidth - 32, 52, COLOR_PANEL_2);
    }

    private void drawPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int fillColor) {
        drawRoundedPanel(guiGraphics, x - 1, y - 1, width + 2, height + 2, fillColor);
    }

    private void drawRoundedPanel(GuiGraphics guiGraphics, int x, int y, int width, int height, int fillColor) {
        int border = COLOR_BORDER_DARK;
        int highlight = COLOR_BORDER_LIGHT;

        // Outer dark rounded border
        guiGraphics.fill(x + 2, y, x + width - 2, y + 1, border);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, border);
        guiGraphics.fill(x, y + 2, x + width, y + height - 2, border);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, border);
        guiGraphics.fill(x + 2, y + height - 1, x + width - 2, y + height, border);

        // Main fill with stepped corners
        guiGraphics.fill(x + 3, y + 1, x + width - 3, y + height - 1, fillColor);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
        guiGraphics.fill(x + 1, y + 3, x + width - 1, y + height - 3, fillColor);

        // Subtle top/left highlight
        guiGraphics.fill(x + 3, y + 1, x + width - 3, y + 2, highlight);
        guiGraphics.fill(x + 1, y + 3, x + 2, y + height - 3, highlight);
    }

    private void drawRoundedTab(GuiGraphics guiGraphics, int x, int y, int width, int height, int fillColor, int borderColor) {
        int highlight = COLOR_BORDER_LIGHT;
        int shadow = COLOR_BORDER_DARK;

        // Border with small rounded corners
        guiGraphics.fill(x + 2, y, x + width - 2, y + 1, borderColor);
        guiGraphics.fill(x + 1, y + 1, x + width - 1, y + 2, borderColor);
        guiGraphics.fill(x, y + 2, x + width, y + height - 2, borderColor);
        guiGraphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, borderColor);
        guiGraphics.fill(x + 2, y + height - 1, x + width - 2, y + height, borderColor);

        // Fill
        guiGraphics.fill(x + 3, y + 1, x + width - 3, y + height - 1, fillColor);
        guiGraphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, fillColor);
        guiGraphics.fill(x + 1, y + 3, x + width - 1, y + height - 3, fillColor);

        // Bevel
        guiGraphics.fill(x + 3, y + 1, x + width - 3, y + 2, highlight);
        guiGraphics.fill(x + 1, y + 3, x + 2, y + height - 3, highlight);

        guiGraphics.fill(x + 2, y + height - 2, x + width - 2, y + height - 1, shadow);
        guiGraphics.fill(x + width - 2, y + 3, x + width - 1, y + height - 3, shadow);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        AdminTab[] tabs = AdminTab.values();

        for (int i = 0; i < tabs.length; i++) {
            AdminTab tab = tabs[i];

            if (isMouseOverTab(mouseX, mouseY, i, tab == this.activeTab)) {
                this.activeTab = tab;
                this.menu.setActiveTabIndex(tab.ordinal());

                if (this.minecraft != null && this.minecraft.gameMode != null) {
                    this.minecraft.gameMode.handleInventoryButtonClick(
                            this.menu.containerId,
                            VendingMachineAdminMenu.TAB_BUTTON_OFFSET + tab.ordinal()
                    );
                }

                rebuildTabButtons();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverTab(double mouseX, double mouseY, int index, boolean selected) {
        TabBounds bounds = getTabBounds(this.leftPos, this.topPos, index, selected);

        return mouseX >= bounds.left
                && mouseX < bounds.right
                && mouseY >= bounds.top
                && mouseY < bounds.bottom;
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
        guiGraphics.drawString(this.font, Component.literal("Items for sale"), 12, 42, 0xA0FFA0, false);

        guiGraphics.drawString(this.font, Component.literal("Output Tray"), 12, 112, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Purchased items"), 12, 122, 0xFFB0B0, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    private void renderSalesLabels(GuiGraphics guiGraphics) {
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
        guiGraphics.drawString(this.font, Component.literal("Payments collected"), 12, 44, 0xFFE0A0, false);

        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFFFFF, false);
    }

    private void renderDisplayLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, Component.literal("Display Settings"), 12, 32, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.literal("Choose machine sign"), 12, 44, 0xA0C8FF, false);

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
                Component.literal("Updates machine display live."),
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
        // Outer dark border
        guiGraphics.fill(x - 1, y - 1, x + 17, y + 17, COLOR_BORDER_DARK);

        // Slot bevel
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF5D6670);
        guiGraphics.fill(x + 1, y + 1, x + 16, y + 16, 0xFF15191E);
        guiGraphics.fill(x + 1, y + 1, x + 15, y + 15, 0xFF2A3038);

        // Inner subtle highlight
        guiGraphics.fill(x + 2, y + 2, x + 14, y + 3, 0xFF3A424C);
    }
}