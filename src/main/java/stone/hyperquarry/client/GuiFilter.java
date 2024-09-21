package stone.hyperquarry.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import stone.hyperquarry.Proxy;
import stone.hyperquarry.common.DropList;
import stone.hyperquarry.common.Filter;
import stone.hyperquarry.network.PacketSetMask;

import java.io.IOException;
import java.util.BitSet;

public class GuiFilter extends GuiScreen {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation(
        "textures/gui/container/generic_54.png");

    private static final int COLUMNS = 32;
    private static final int ROWS = 16;
    private static final int ITEM_SIZE = 18;

    /** The X size of the inventory window in pixels. */
    protected int xSize = COLUMNS * ITEM_SIZE + 17;
    /** The Y size of the inventory window in pixels. */
    protected int ySize = ROWS * ITEM_SIZE + 17;

    private GuiButton cancelButton;
    private GuiButton confirmButton;

    private BlockPos target;
    private int dimension;

    private DropList list;
    private Filter filter;

    private GuiQuarry quarryGui;

    public GuiFilter(GuiQuarry quarryGui, Filter filter) {
        this.quarryGui = quarryGui;
        this.target = quarryGui.getSource();
        this.dimension = filter.dimension();

        this.list = DropList.of(filter);
        this.filter = filter;
    }

    @Override
    public void initGui() {
        super.initGui();

        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2 + this.ySize;

        this.cancelButton = this
            .addButton(new GuiButton(0, startX, startY, this.xSize / 2, 20, "Cancel"));
        this.confirmButton = this
            .addButton(
                new GuiButton(1, startX + this.xSize / 2, startY, this.xSize / 2, 20, "Confirm"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        // this.drawTexturedModalRect(i, j, 0, 0, this.xSize, 6 * 18 + 17);
        this.drawFilter();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawFilter() {
        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;
        for (int j = 0; j < ROWS; j++)
        {
        for (int i = 0; i < COLUMNS; i++)
        {
            
                int x = startX + i * ITEM_SIZE;
                int y = startY + j * ITEM_SIZE;
                if (list.items().length > flatten(i, j))
                {
                    this.itemRender.renderItemIntoGUI(list.getStack(flatten(i, j), (byte) 1), x, y);
                    if (this.filter.mask().get(flatten(i, j)))
                    {
                        Gui.drawRect(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xA000FF00);
                    } else
                    {
                        Gui.drawRect(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xA0FF0000);
                    }
                } else
                {
                    Gui.drawRect(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xA0000000);
                }
            }
            if (list.items().length < flatten(0, j))
                return;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        // normalize to be in the space of the 2d arrow of items
        int startX = (this.width - this.xSize) / 2;
        int startY = (this.height - this.ySize) / 2;

        int normalizedX = (mouseX - startX) / ITEM_SIZE;
        int normalizedY = (mouseY - startY) / ITEM_SIZE;

        if (normalizedX >= 0 && normalizedX < COLUMNS)
        {
            if (normalizedY >= 0 && normalizedY < ROWS)
            {
                this.filter.mask()
                    .set(flatten(normalizedX, normalizedY),
                        !this.filter.mask().get(flatten(normalizedX, normalizedY)));
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
            this.mc.displayGuiScreen(quarryGui);
        }
        else if (keyCode == 1)
        { this.mc.player.closeScreen(); }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button == this.cancelButton)
        {
            this.mc.displayGuiScreen(quarryGui);
        } else if (button == this.confirmButton)
        {
            Proxy.NETWORK
                .sendToServer(
                    new PacketSetMask(filter, this.target));
            this.mc.player.closeScreen();
        }
    }

    private int flatten(int i, int j) { return i + j * COLUMNS; }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
