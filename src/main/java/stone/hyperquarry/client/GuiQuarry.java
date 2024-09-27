package stone.hyperquarry.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.math.BlockPos;

import stone.hyperquarry.Proxy;
import stone.hyperquarry.common.Filter;
import stone.hyperquarry.network.PacketOpenFilterGui;
import stone.hyperquarry.network.PacketSetMask;
import stone.hyperquarry.network.PacketSetQuarryState;

import java.io.IOException;
import java.util.BitSet;

public class GuiQuarry extends GuiScreen {
    private final BlockPos source;

    private int sizeX = 250;
    private int sizeY = 100;
    private GuiButton filter;
    private GuiButton toggleRunning;
    private GuiButton confirm;

    private GuiTextField dimensionList;
    private GuiTextField enchantsList;

    private long mined;
    private boolean isRunning;
    private int cost;

    public GuiQuarry(BlockPos source, long mined, boolean isRunning, int cost) {
        this.source = source;
        this.mined = mined;
        this.isRunning = isRunning;
        this.cost = cost;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        int startX = (this.width - this.sizeX) / 2;
        int startY = (this.height - this.sizeY) / 2;

        this.drawString(fontRenderer, "Total Items Mined: " + mined, startX, startY, 0xFFFFFFFF);
        this.drawString(fontRenderer, "Current Cost per Item: " + cost, startX, startY + 20, 0xFFFFFF);
        this.drawString(fontRenderer, "Enchants Name:", startX, startY + 40, 0xFFFFFFFF);
        if (mouseX > startX && mouseX < startX + this.sizeX / 2) {
            if (mouseY > startY + 40 && mouseY < startY + 60) {
                this.drawHoveringText("Valid Names: \"drops\", \"smelts\", \"silks\"", mouseX, mouseY);
            }
        }

        if (mouseX > startX + sizeX / 2 && mouseX < startX + this.sizeX) {
            if (mouseY > startY + 40 && mouseY < startY + 60) {
                this.drawHoveringText("Enter a dimension id like 0, 1, -9999, /forge tps can help to find ids", mouseX,
                        mouseY);
            }
        }

        this.enchantsList.drawTextBox();
        this.drawString(fontRenderer, "Dimension Id:", startX + this.sizeX / 2, startY + 40, 0xFFFFFFFF);
        this.dimensionList.drawTextBox();
    }

    @Override
    public void initGui() {
        super.initGui();
        int id = 0;

        int startX = (this.width - this.sizeX) / 2;
        int startY = (this.height - this.sizeY) / 2;
        this.filter = addButton(new GuiButton(id++, startX, startY + this.sizeY, this.sizeX / 2, 20, "Open Filter"));
        this.toggleRunning = addButton(new GuiButton(id++, startX + this.sizeX / 2, startY + this.sizeY, this.sizeX / 2,
                20, isRunning ? "Stop" : "Start"));
        this.confirm = addButton(new GuiButton(id++, startX, startY + 80, this.sizeX, 20, "Confirm Drop List"));
        ;
        this.enchantsList = new GuiTextField(id++, fontRenderer, startX, startY + 60, this.sizeX / 2, 20);

        this.dimensionList = new GuiTextField(id++, fontRenderer, startX + this.sizeX / 2, startY + 60, this.sizeX / 2,
                20);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button == this.filter) {
            Proxy.NETWORK.sendToServer(new PacketOpenFilterGui(this.source));
            this.isRunning = false;
        } else if (button == this.toggleRunning) {
            isRunning = !isRunning;
            button.displayString = isRunning ? "Stop" : "Start";
            Proxy.NETWORK.sendToServer(new PacketSetQuarryState(source, isRunning));
        } else if (button == this.confirm) {
            try {
                Proxy.NETWORK.sendToServer(new PacketSetMask(
                        new Filter(enchantsList.getText(), Integer.valueOf(dimensionList.getText()), new BitSet()),
                        source));
                this.isRunning = false;
            } catch (NumberFormatException e) {
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.enchantsList.textboxKeyTyped(typedChar, keyCode);
        this.dimensionList.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == 1) {
            this.mc.player.closeScreen();
        }
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.enchantsList.mouseClicked(mouseX, mouseY, mouseButton);
        this.dimensionList.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public BlockPos getSource() {
        return this.source;
    }
}
