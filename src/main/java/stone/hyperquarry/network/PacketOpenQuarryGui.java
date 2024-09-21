package stone.hyperquarry.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import stone.hyperquarry.client.GuiQuarry;
import stone.hyperquarry.common.TileEntityQuarry;

public class PacketOpenQuarryGui extends PacketTargeted {
    private long mined;
    private boolean isRunning;
    private int cost;

    public PacketOpenQuarryGui() { super(); }
    public PacketOpenQuarryGui(TileEntityQuarry quarry) {
        super(quarry.getPos());
        this.mined = quarry.getMined();
        this.isRunning = quarry.isRunning();
        this.cost = quarry.getCost();        
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);

        this.mined = buf.readLong();
        this.isRunning = buf.readBoolean();
        this.cost = buf.readInt();
        }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);

        buf.writeLong(this.mined);
        buf.writeBoolean(this.isRunning);
        buf.writeInt(this.cost);
        }

    public static class ClientHandler implements IMessageHandler<PacketOpenQuarryGui, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenQuarryGui message, MessageContext ctx) {
            Minecraft
                .getMinecraft()
                .addScheduledTask(() -> Minecraft
                    .getMinecraft()
                    .displayGuiScreen(
                        new GuiQuarry(message.target, message.mined, message.isRunning, message.cost)));
                
            return null;
        }

    }
}
